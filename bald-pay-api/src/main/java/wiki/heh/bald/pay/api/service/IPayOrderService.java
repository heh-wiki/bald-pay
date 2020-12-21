package wiki.heh.bald.pay.api.service;

import com.alibaba.fastjson.JSONObject;
import wiki.heh.bald.pay.api.entity.vo.Result;

import java.util.Map;

/**
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
public interface IPayOrderService {

    Map createPayOrder(String jsonParam);

    Map selectPayOrder(String jsonParam);

    Map selectPayOrderByMchIdAndPayOrderId(String jsonParam);

    Map selectPayOrderByMchIdAndMchOrderNo(String jsonParam);

    Map updateStatus4Ing(String jsonParam);

    Map updateStatus4Success(String jsonParam);

    Map updateStatus4Complete(String jsonParam);

    Map updateNotify(String jsonParam);

    int createPayOrder(JSONObject payOrder);

    JSONObject queryPayOrder(String mchId, String payOrderId, String mchOrderNo, String executeNotify);

    Result doWxPayReq(String tradeType, JSONObject payOrder, String resKey);

    Result doAliPayReq(String channelId, JSONObject payOrder, String resKey);

}
