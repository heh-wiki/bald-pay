package wiki.heh.bald.pay.api.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import wiki.heh.bald.pay.api.service.BaseService4RefundOrder;

import javax.jms.Queue;

/**
 * 商户通知MQ统一处理
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Component
public class Mq4MchRefundNotify extends Mq4MchNotify {
    private static final Integer NOTIFY_MAX = 12;
    @Autowired
    private Queue mchRefundNotifyQueue;

    @Autowired
    private BaseService4RefundOrder baseService4RefundOrder;

    private static final Logger _log = LoggerFactory.getLogger(Mq4MchRefundNotify.class);

    public void send(String msg) {
        super.send(new ActiveMQQueue(MqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME), msg);
    }

    @JmsListener(destination = MqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME)
    public void receive(String msg) {
        String logPrefix = "【商户退款通知】";
        _log.info("{}接收消息:msg={}", logPrefix, msg);
        JSONObject msgObj = JSON.parseObject(msg);
        String respUrl = msgObj.getString("url");
        String orderId = msgObj.getString("orderId");
        int count = msgObj.getInteger("count");
        if (StringUtils.isEmpty(respUrl)) {
            _log.warn("{}商户通知URL为空,respUrl={}", logPrefix, respUrl);
            return;
        }
        String httpResult = httpPost(respUrl);
        count++;
        long time = ((count<<count)>>1) * 60 * 1000;
        _log.info("{}notifyCount={}", logPrefix, count);
        if ("success".equalsIgnoreCase(httpResult)) {
            // 修改支付订单表
            try {
                int result = baseService4RefundOrder.baseUpdateStatus4Complete(orderId);
                _log.info("{}修改payOrderId={},订单状态为处理完成->{}", logPrefix, orderId, result == 1 ? "成功" : "失败");
            } catch (Exception e) {
                _log.error("修改订单状态为处理完成异常");
            }
            // 修改通知
            try {
                int result = super.baseUpdateMchNotifySuccess(orderId, httpResult, (byte) count);
                _log.info("{}修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, httpResult, count, result == 1 ? "成功" : "失败");
            } catch (Exception e) {
                _log.error("修改商户支付通知异常");
            }
            return; // 通知成功结束
        } else {
            // 修改通知次数
            try {
                int result = super.baseUpdateMchNotifyFail(orderId, httpResult, (byte) count);
                _log.info("{}修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, httpResult, count, result == 1 ? "成功" : "失败");
            } catch (Exception e) {
                _log.error("修改商户支付通知异常");
            }
            if (count > NOTIFY_MAX) {
                _log.info("{}通知次数notifyCount({})>{},停止通知", respUrl, count, NOTIFY_MAX);
                return;
            }
            // 通知失败，延时再通知
            msgObj.put("count", count);
            this.send(mchRefundNotifyQueue, msgObj.toJSONString(), time);
            _log.info("{}发送延时通知完成,通知次数:{},{}后执行通知", respUrl, count, time/1000/60<60?time+"分钟":(time/1000/60/60<24? time/1000/60/60+"小时:"+time/1000/60%60+"分钟":(time/1000/60/60)/24+"天"));
        }
    }

    public static void main(String[] args) {
        int con = 0;
        System.out.println("支付中心回调时刻表");
        for (int i = 1; i < 20; i++) {
            int c = (i<<i)>>1;
            System.out.println("第"+i+"次回调时间间隔->".concat(c<60?c+"分钟":(c/60<24? c/60+"小时:"+c%60+"分钟":(c/60)/24+"天")));
            con+=c;
        }
        System.out.println("超过"+con/60/24+"天后不再有回调");
    }
}
