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
    Map doAliPayMobileReq(String jsonParam);

    /**
     * 当面付(二维码支付)
     *
     * @param jsonParam
     * @return
     */
    Result doAliPayQrReq(String jsonParam, String resKey);

    Map doAliTransReq(String jsonParam);

    Map getAliTransReq(String jsonParam);

    Map doAliRefundReq(String jsonParam);

    Map getAliRefundReq(String jsonParam);

}
