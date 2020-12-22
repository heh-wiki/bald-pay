package wiki.heh.bald.pay.api.mq;

import org.springframework.context.annotation.Configuration;


/**
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Configuration
public class MqConfig {

    public static final String PAY_NOTIFY_QUEUE_NAME = "pay.notify.queue";

    public static final String PAY_NOTIFY_EXCHANGE_NAME = "pay.notify.exchange";

    public static final String MCH_NOTIFY_QUEUE_NAME = "queue.notify.mch";

    public static final String MCH_PAY_NOTIFY_QUEUE_NAME = "queue.notify.mch.pay";

    public static final String MCH_TRANS_NOTIFY_QUEUE_NAME = "queue.notify.mch.trans";

    public static final String MCH_REFUND_NOTIFY_QUEUE_NAME = "queue.notify.mch.refund";

    public static final String TRANS_NOTIFY_QUEUE_NAME = "queue.notify.trans";

    public static final String REFUND_NOTIFY_QUEUE_NAME = "queue.notify.refund";

    public static class Impl {
        public static final String ACTIVE_MQ = "activeMQ";
        public static final String RABBIT_MQ = "rabbitMQ";
    }

}
