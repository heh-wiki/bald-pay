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
import wiki.heh.bald.pay.api.entity.po.TransOrder;
import wiki.heh.bald.pay.api.service.BaseNotify4MchTransfer;
import wiki.heh.bald.pay.api.service.BaseService4TransferOrder;
import wiki.heh.bald.pay.api.service.IPayChannel4AliService;
import wiki.heh.bald.pay.api.service.IPayChannel4WxService;
import wiki.heh.bald.pay.common.constant.PayConstant;
import wiki.heh.bald.pay.common.util.RpcUtil;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 *  业务通知MQ实现
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Component
public class Mq4TransferNotify extends BaseService4TransferOrder {

    @Autowired
    private Queue transNotifyQueue;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private IPayChannel4WxService payChannel4WxService;

    @Autowired
    private IPayChannel4AliService payChannel4AliService;

    @Autowired
    private BaseNotify4MchTransfer baseNotify4MchTrans;

    private static final Logger _log = LoggerFactory.getLogger(Mq4TransferNotify.class);

    public void send(String msg) {
        _log.info("发送MQ消息:msg={}", msg);
        this.jmsTemplate.convertAndSend(this.transNotifyQueue, msg);
    }

    /**
     * 发送延迟消息
     * @param msg
     * @param delay
     */
    public void send(String msg, long delay) {
        _log.info("发送MQ延时消息:msg={},delay={}", msg, delay);
        jmsTemplate.send(this.transNotifyQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage tm = session.createTextMessage(msg);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1*1000);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
                return tm;
            }
        });
    }

    @JmsListener(destination = MqConfig.TRANS_NOTIFY_QUEUE_NAME)
    public void receive(String msg) {
        _log.info("处理转账任务.msg={}", msg);
        JSONObject msgObj = JSON.parseObject(msg);
        String transOrderId = msgObj.getString("transOrderId");
        String channelName = msgObj.getString("channelName");
        TransOrder transOrder = baseSelectTransOrder(transOrderId);
        if(transOrder == null) {
            _log.warn("查询转账订单为空,不能转账.transOrderId={}", transOrderId);
            return;
        }
        if(transOrder.getStatus() != PayConstant.TRANS_STATUS_INIT) {
            _log.warn("转账状态不是初始({})或失败({}),不能转账.transOrderId={}", PayConstant.TRANS_STATUS_INIT, PayConstant.TRANS_STATUS_FAIL, transOrderId);
            return;
        }
        int result = this.baseUpdateStatus4Ing(transOrderId, "");
        if(result != 1) {
            _log.warn("更改转账为转账中({})失败,不能转账.transOrderId={}", PayConstant.TRANS_STATUS_TRANING, transOrderId);
            return;
        }
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("transOrder", transOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map resultMap;
        if(PayConstant.CHANNEL_NAME_WX.equalsIgnoreCase(channelName)) {
            resultMap = payChannel4WxService.doWxTransReq(jsonParam);
        }else if(PayConstant.CHANNEL_NAME_ALIPAY.equalsIgnoreCase(channelName)) {
            resultMap = payChannel4AliService.doAliTransReq(jsonParam);
        }else {
            _log.warn("不支持的转账渠道,停止转账处理.transOrderId={},channelName={}", transOrderId, channelName);
            return;
        }
        if(!RpcUtil.isSuccess(resultMap)) {
            _log.warn("发起转账返回异常,停止转账处理.transOrderId={}", transOrderId);
            return;
        }
        Map bizResult = (Map) resultMap.get("bizResult");
        Boolean isSuccess = false;
        if(bizResult.get("isSuccess") != null) isSuccess = Boolean.parseBoolean(bizResult.get("isSuccess").toString());
        if(isSuccess) {
            // 更新转账状态为成功
            String channelOrderNo = bizResult.get("channelOrderNo") == null ? "" : bizResult.get("channelOrderNo").toString();
            result = baseUpdateStatus4Success(transOrderId, channelOrderNo);
            _log.info("更新转账订单状态为成功({}),transOrderId={},返回结果:{}", PayConstant.TRANS_STATUS_SUCCESS, transOrderId, result);
            // 发送商户通知
            baseNotify4MchTrans.doNotify(transOrder, true);
        }else {
            // 更新转账状态为失败
            String channelErrCode = bizResult.get("channelErrCode") == null ? "" : bizResult.get("channelErrCode").toString();
            String channelErrMsg = bizResult.get("channelErrMsg") == null ? "" : bizResult.get("channelErrMsg").toString();
            result = baseUpdateStatus4Fail(transOrderId, channelErrCode, channelErrMsg);
            _log.info("更新转账订单状态为失败({}),transOrderId={},返回结果:{}", PayConstant.TRANS_STATUS_FAIL, transOrderId, result);
            // 发送商户通知
            baseNotify4MchTrans.doNotify(transOrder, true);
        }

    }
}
