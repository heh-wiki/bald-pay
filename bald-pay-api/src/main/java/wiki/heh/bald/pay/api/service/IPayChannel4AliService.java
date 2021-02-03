package wiki.heh.bald.pay.api.service;

import wiki.heh.bald.pay.api.entity.vo.Result;

import java.util.Map;

/**
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
public interface IPayChannel4AliService {
    /**
     * WAP支付
     *
     * @param jsonParam
     * @return
     */
    Map doAliPayWapReq(String jsonParam);

    /**
     * PC支付
     *
     * @param jsonParam
     * @return
     */
    Map doAliPayPcReq(String jsonParam);

    /**
     * 手机支付
     *
     * @param jsonParam
     * @return
     */
    Result doAliPayMobileReq(String jsonParam, String resKey);

    /**
     * 当面付(二维码支付)
     *
     * @param jsonParam
     * @return
     */
    Result doAliPayQrReq(String jsonParam, String resKey);

    /**
     * 当面付(二维码支付)
     *
     * @param jsonParam
     * @return
     */
    Result doAliPayJsApiReq(String jsonParam, String resKey);

    /**
     * 转账
     *
     * @param jsonParam
     * @return
     */
    Map doAliTransReq(String jsonParam);

    Map getAliTransReq(String jsonParam);

    /**
     * 交易退款
     *
     * @param jsonParam
     * @return
     */
    Map doAliRefundReq(String jsonParam);

    Map getAliRefundReq(String jsonParam);

}
