package wiki.heh.bald.pay.api.service;

import java.util.Map;

/**
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
public interface IPayChannel4WxService {



    Map doWxPayReq(String jsonParam);

    Map doWxTransReq(String jsonParam);

    Map getWxTransReq(String jsonParam);

    Map doWxRefundReq(String jsonParam);

    Map getWxRefundReq(String jsonParam);

}
