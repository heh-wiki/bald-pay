package wiki.heh.bald.pay.api.service;

import java.util.Map;

/**
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
public interface INotifyPayService {

    Map doAliPayNotify(String jsonParam);

    Map doWxPayNotify(String jsonParam);

    Map sendBizPayNotify(String jsonParam);

    /**
     * 处理支付宝回调
     *
     * @param params
     * @return
     */
    String handleAliPayNotify(Map params);

    /**
     * 处理微信回调
     *
     * @param xmlResult
     * @return
     */
    String handleWxPayNotify(String xmlResult);
}
