package wiki.heh.bald.pay.api.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import wiki.heh.bald.pay.api.entity.po.RefundOrder;
import wiki.heh.bald.pay.api.service.BaseNotify4MchRefund;
import wiki.heh.bald.pay.api.service.BaseService4RefundOrder;
import wiki.heh.bald.pay.api.service.IPayChannel4AliService;
import wiki.heh.bald.pay.api.service.IPayChannel4WxService;
import wiki.heh.bald.pay.common.constant.PayConstant;
import wiki.heh.bald.pay.common.util.RpcUtil;
import wiki.heh.bald.pay.common.util.StrUtil;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

import static wiki.heh.bald.pay.api.mq.MqConfig.PAY_NOTIFY_QUEUE_NAME;

/**
 * 业务通知MQ实现
 *
 * @author hehua
 * @version v1.0
 * @date 2020/12/18
 */
@Component
public class Mq4RefundNotify extends BaseService4RefundOrder {

    @Autowired
    private Queue refundNotifyQueue;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private IPayChannel4WxService payChannel4WxService;

    @Autowired
    private IPayChannel4AliService payChannel4AliService;

    @Autowired
    private BaseNotify4MchRefund baseNotify4MchRefund;

    private static final Logger _log = LoggerFactory.getLogger(Mq4RefundNotify.class);

    public void send(String msg) {
        _log.info("发送MQ消息:msg={}", msg);
        this.jmsTemplate.convertAndSend(new ActiveMQQueue(MqConfig.REFUND_NOTIFY_QUEUE_NAME), msg);
    }

    /**
     * 发送延迟消息
     *
     * @param msg
     * @param delay
     */
    public void send(String msg, long delay) {
        _log.info("发送MQ延时消息:msg={},delay={}", msg, delay);
        jmsTemplate.send(this.refundNotifyQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage tm = session.createTextMessage(msg);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1 * 1000);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
                return tm;
            }
        });
    }

    @JmsListener(destination = MqConfig.REFUND_NOTIFY_QUEUE_NAME)
    public void receive(String msg) {
        _log.info("处理退款任务.msg={}", msg);
        JSONObject msgObj = JSON.parseObject(msg);
        String refundOrderId = msgObj.getString("refundOrderId");
        String channelName = msgObj.getString("channelName");
        RefundOrder refundOrder = baseSelectRefundOrder(refundOrderId);
        if (refundOrder == null) {
            _log.warn("查询退款订单为空,不能退款.refundOrderId={}", refundOrderId);
            return;
        }
        if (refundOrder.getStatus() != PayConstant.REFUND_STATUS_INIT) {
            _log.warn("退款状态不是初始({})或失败({}),不能退款.refundOrderId={}", PayConstant.REFUND_STATUS_INIT, PayConstant.REFUND_STATUS_FAIL, refundOrderId);
            return;
        }
        int result = this.baseUpdateStatus4Ing(refundOrderId, "");
        if (result != 1) {
            _log.warn("更改退款为退款中({})失败,不能退款.refundOrderId={}", PayConstant.REFUND_STATUS_REFUNDING, refundOrderId);
            return;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("refundOrder", refundOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map resultMap;
        if (PayConstant.CHANNEL_NAME_WX.equalsIgnoreCase(channelName)) {
            resultMap = payChannel4WxService.doWxRefundReq(jsonParam);
        } else if (PayConstant.CHANNEL_NAME_ALIPAY.equalsIgnoreCase(channelName)) {
            resultMap = payChannel4AliService.doAliRefundReq(jsonParam);
        } else {
            _log.warn("不支持的退款渠道,停止退款处理.refundOrderId={},channelName={}", refundOrderId, channelName);
            return;
        }
        if (!RpcUtil.isSuccess(resultMap)) {
            _log.warn("发起退款返回异常,停止退款处理.refundOrderId={}", refundOrderId);
            return;
        }
        Map bizResult = (Map) resultMap.get("bizResult");
        Boolean isSuccess = false;
        if (bizResult.get("isSuccess") != null) isSuccess = Boolean.parseBoolean(bizResult.get("isSuccess").toString());
        if (isSuccess) {
            // 更新退款状态为成功
            String channelOrderNo = StrUtil.toString(bizResult.get("channelOrderNo"));
            result = baseUpdateStatus4Success(refundOrderId, channelOrderNo);
            _log.info("更新退款订单状态为成功({}),refundOrderId={},返回结果:{}", PayConstant.REFUND_STATUS_SUCCESS, refundOrderId, result);
            // 发送商户通知
            baseNotify4MchRefund.doNotify(refundOrder, true);
        } else {
            // 更新退款状态为失败
            String channelErrCode = StrUtil.toString(bizResult.get("channelErrCode"));
            String channelErrMsg = StrUtil.toString(bizResult.get("channelErrMsg"));
            result = baseUpdateStatus4Fail(refundOrderId, channelErrCode, channelErrMsg);
            _log.info("更新退款订单状态为失败({}),refundOrderId={},返回结果:{}", PayConstant.REFUND_STATUS_FAIL, refundOrderId, result);
            // 发送商户通知
            baseNotify4MchRefund.doNotify(refundOrder, true);
        }

    }
}
