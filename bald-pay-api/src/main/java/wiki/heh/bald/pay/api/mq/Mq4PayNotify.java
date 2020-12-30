package wiki.heh.bald.pay.api.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import wiki.heh.bald.pay.api.service.BaseService4PayOrder;

import javax.jms.*;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

import static wiki.heh.bald.pay.api.mq.MqConfig.PAY_NOTIFY_QUEUE_NAME;

/**
 * 业务通知MQ实现
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Component
public class Mq4PayNotify extends BaseService4PayOrder {

    @Autowired
    private Queue payNotifyQueue;
    private static final Integer NOTIFY_MAX = 1000;
    @Autowired
    private JmsTemplate jmsTemplate;

    private static final Logger _log = LoggerFactory.getLogger(Mq4PayNotify.class);

    public void send(String msg) {
        _log.info("发送MQ消息:msg={}", msg);
        this.jmsTemplate.convertAndSend(this.payNotifyQueue, msg);
    }

    /**
     * 发送延迟消息
     *
     * @param msg
     * @param delay
     */
    public void send(String msg, long delay) {
        _log.info("发送MQ延时消息:msg={},delay={}", msg, delay);
        jmsTemplate.send(this.payNotifyQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage tm = session.createTextMessage(msg);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1 * 1000);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
                return tm;
            }
        });
    }

    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

    @JmsListener(destination = PAY_NOTIFY_QUEUE_NAME)
    public void receive(String msg) {
        _log.info("do notify task, msg={}", msg);
        JSONObject msgObj = JSON.parseObject(msg);
        String respUrl = msgObj.getString("url");
        String orderId = msgObj.getString("orderId");
        int count = msgObj.getInteger("count");
        if (StringUtils.isEmpty(respUrl)) {
            _log.warn("notify url is empty. respUrl={}", respUrl);
            return;
        }
        try {
            StringBuffer sb = new StringBuffer();
            URL console = new URL(respUrl);
            _log.info("==>MQ通知业务系统开始[orderId：{}][count：{}][time：{}]", orderId, count, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            if ("https".equals(console.getProtocol())) {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[]{new TrustAnyTrustManager()},
                        new java.security.SecureRandom());
                HttpsURLConnection con = (HttpsURLConnection) console.openConnection();
                con.setSSLSocketFactory(sc.getSocketFactory());
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setConnectTimeout(10 * 1000);
                con.setReadTimeout(5 * 1000);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()), 1024 * 1024);
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                }
                in.close();
            } else if ("http".equals(console.getProtocol())) {
                HttpURLConnection con = (HttpURLConnection) console.openConnection();
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setConnectTimeout(10 * 1000);
                con.setReadTimeout(5 * 1000);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()), 1024 * 1024);
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                }
                in.close();
            } else {
                _log.error("not do protocol. protocol={}", console.getProtocol());
                return;
            }
            _log.info("<==MQ通知业务系统结束[orderId：{}][count：{}][time：{}]", orderId, count, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            // 验证结果
            _log.info("notify response , OrderID={}", orderId);
            if (sb.toString().trim().equalsIgnoreCase("success")) {
                //_log.info("{} notify success, url:{}", _notifyInfo.getBusiId(), respUrl);
                //修改订单表
                try {
                    int result = super.baseUpdateStatus4Complete(orderId);
                    _log.info("修改payOrderId={},订单状态为处理完成->{}", orderId, result == 1 ? "成功" : "失败");
                } catch (Exception e) {
                    _log.error("修改订单状态为处理完成异常");
                    e.printStackTrace();
                }
                // 修改通知次数
                try {
                    int result = super.baseUpdateNotify(orderId, (byte) (1 + count));
                    _log.info("修改payOrderId={},通知业务系统次数->{},状态：{}", orderId, count, result == 1 ? "成功" : "失败");
                } catch (Exception e) {
                    _log.error("修改通知次数异常");
                    e.printStackTrace();
                }
                return; // 通知成功结束
            } else {
                // 通知失败，延时再通知
                count++;
                _log.info("notify count={}", count);
                // 修改通知次数
                try {
                    int result = super.baseUpdateNotify(orderId, (byte) count);
                    _log.info("修改payOrderId={},通知业务系统次数->{}", orderId, result == 1 ? "成功" : "失败");
                } catch (Exception e) {
                    _log.error("修改通知次数异常");
                    e.printStackTrace();
                }

                if (count > NOTIFY_MAX) {
                    _log.info("notify count>{} stop. url={}", NOTIFY_MAX, respUrl);
                    return;
                }
                msgObj.put("count", count);
                this.send(msgObj.toJSONString(), count * 60 * 1000);
            }
            _log.warn("notify failed. url:{}, response body:{}", respUrl, sb.toString());
        } catch (Exception e) {
            _log.info("<==MQ通知业务系统结束[orderId：{}][count：{}][time：{}]", orderId, count, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            _log.error("notify exception. url:%s", respUrl);
            e.printStackTrace();
        }

    }
}
