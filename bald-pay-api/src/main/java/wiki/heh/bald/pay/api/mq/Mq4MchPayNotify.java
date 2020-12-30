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
import org.springframework.web.client.RestTemplate;
import wiki.heh.bald.pay.api.service.BaseService;
import wiki.heh.bald.pay.api.service.BaseService4PayOrder;

import javax.jms.Queue;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import static wiki.heh.bald.pay.api.mq.MqConfig.MCH_PAY_NOTIFY_QUEUE_NAME;

/**
 * 商户通知MQ统一处理
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Component
public class Mq4MchPayNotify extends Mq4MchNotify {

    @Autowired
    private Queue mchPayNotifyQueue;
    private static final Integer NOTIFY_MAX = 12;
    @Autowired
    private BaseService4PayOrder baseService4PayOrder;

    private static final Logger _log = LoggerFactory.getLogger(Mq4MchPayNotify.class);

    public void send(String msg) {
        super.send(new ActiveMQQueue(MCH_PAY_NOTIFY_QUEUE_NAME), msg);
    }

    @JmsListener(destination = MCH_PAY_NOTIFY_QUEUE_NAME)
    public void receive(String msg) {
        String logPrefix = "【商户支付通知】";
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
                int result = baseService4PayOrder.baseUpdateStatus4Complete(orderId);
                _log.info("{}修改payOrderId={},订单状态为处理完成->{}", logPrefix, orderId, result == 1 ? "成功" : "失败");
            } catch (Exception e) {
                _log.error("修改订单状态为处理完成异常");
                e.printStackTrace();
            }
            // 修改通知
            try {
                int result = super.baseUpdateMchNotifySuccess(orderId, httpResult, (byte) count);
                _log.info("{}修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, httpResult, count, result == 1 ? "成功" : "失败");
            } catch (Exception e) {
                _log.error("修改商户支付通知异常");
                e.printStackTrace();
            }
            return; // 通知成功结束
        } else {
            // 修改通知次数
            try {
                int result = super.baseUpdateMchNotifyFail(orderId, httpResult, (byte) count);
                _log.info("{}修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, httpResult, count, result == 1 ? "成功" : "失败");
            } catch (Exception e) {
                _log.error("修改商户支付通知异常");
                e.printStackTrace();
            }
            if (count > NOTIFY_MAX) {
                _log.info("{}通知次数notifyCount()>5,停止通知", respUrl, count);
                return;
            }
            // 通知失败，延时再通知
            msgObj.put("count", count);
            this.send(mchPayNotifyQueue, msgObj.toJSONString(), time);
            _log.info("{}发送延时通知完成,通知次数:{},{}后执行通知", respUrl, count, time/1000/60<60?time+"分钟":(time/1000/60/60<24? time/1000/60/60+"小时:"+time/1000/60%60+"分钟":(time/1000/60/60)/24+"天"));
        }
    }
}
