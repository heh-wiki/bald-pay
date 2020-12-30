package wiki.heh.bald.pay.api.mq;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.Queue;


/**
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Configuration
public class MqConfig {

    public static final String MCH_NOTIFY_QUEUE_NAME = "queue.notify.mch";

    public static final String MCH_PAY_NOTIFY_QUEUE_NAME = "queue.notify.mch.pay";

    public static final String MCH_TRANS_NOTIFY_QUEUE_NAME = "queue.notify.mch.trans";

    public static final String MCH_REFUND_NOTIFY_QUEUE_NAME = "queue.notify.mch.refund";

    public static final String PAY_NOTIFY_QUEUE_NAME = "queue.notify.pay";

    public static final String TRANS_NOTIFY_QUEUE_NAME = "queue.notify.trans";

    public static final String REFUND_NOTIFY_QUEUE_NAME = "queue.notify.refund";

    @Bean
    public Queue payNotifyQueue() {
        return new ActiveMQQueue(PAY_NOTIFY_QUEUE_NAME);
    }

    @Bean
    public Queue mchNotifyQueue() {
        return new ActiveMQQueue(MCH_NOTIFY_QUEUE_NAME);
    }

    @Bean
    public Queue transNotifyQueue() {
        return new ActiveMQQueue(TRANS_NOTIFY_QUEUE_NAME);
    }

    @Bean
    public Queue refundNotifyQueue() {
        return new ActiveMQQueue(REFUND_NOTIFY_QUEUE_NAME);
    }

    @Bean
    public Queue mchPayNotifyQueue() {
        return new ActiveMQQueue(MCH_PAY_NOTIFY_QUEUE_NAME);
    }

    @Bean
    public Queue mchTransNotifyQueue() {
        return new ActiveMQQueue(MCH_TRANS_NOTIFY_QUEUE_NAME);
    }

    @Bean
    public Queue mchRefundNotifyQueue() {
        return new ActiveMQQueue(MCH_REFUND_NOTIFY_QUEUE_NAME);
    }

}
