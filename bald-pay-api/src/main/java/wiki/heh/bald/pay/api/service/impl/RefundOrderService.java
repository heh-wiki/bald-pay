package wiki.heh.bald.pay.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wiki.heh.bald.pay.api.service.INotifyPayService;
import wiki.heh.bald.pay.api.service.IPayChannel4AliService;
import wiki.heh.bald.pay.api.service.IRefundOrderService;
import wiki.heh.bald.pay.common.constant.PayConstant;
import wiki.heh.bald.pay.common.util.BaldPayUtil;
import wiki.heh.bald.pay.common.util.RpcUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 退款订单service
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Service
public class RefundOrderService {

    private final Logger _log = LoggerFactory.getLogger(RefundOrderService.class);
    @Autowired
    IPayChannel4AliService payChannel4AliService;
    @Autowired
    INotifyPayService notifyPayService;
    @Autowired
    IRefundOrderService refundOrderService;

    public int create(JSONObject refundOrder) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("refundOrder", refundOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map<String, Object> result = refundOrderService.create(jsonParam);
        String s = RpcUtil.mkRet(result);
        if (s == null) return 0;
        return Integer.parseInt(s);
    }

    public void sendRefundNotify(String refundOrderId, String channelName) {
        JSONObject object = new JSONObject();
        object.put("refundOrderId", refundOrderId);
        object.put("channelName", channelName);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("msg", object);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        refundOrderService.sendRefundNotify(jsonParam);
    }

    public JSONObject query(String mchId, String refundOrderId, String mchRefundNo, String executeNotify) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> result;
        if (StringUtils.isNotBlank(refundOrderId)) {
            paramMap.put("mchId", mchId);
            paramMap.put("refundOrderId", refundOrderId);
            String jsonParam = RpcUtil.createBaseParam(paramMap);
            result = refundOrderService.selectByMchIdAndRefundOrderId(jsonParam);
        } else {
            paramMap.put("mchId", mchId);
            paramMap.put("mchRefundNo", mchRefundNo);
            String jsonParam = RpcUtil.createBaseParam(paramMap);
            result = refundOrderService.selectByMchIdAndMchRefundNo(jsonParam);
        }
        String s = RpcUtil.mkRet(result);
        if (s == null) return null;
        boolean isNotify = Boolean.parseBoolean(executeNotify);
        JSONObject payOrder = JSONObject.parseObject(s);
        if (isNotify) {
            paramMap = new HashMap<>();
            paramMap.put("refundOrderId", refundOrderId);
            String jsonParam = RpcUtil.createBaseParam(paramMap);
            result = notifyPayService.sendBizPayNotify(jsonParam);
            s = RpcUtil.mkRet(result);
            _log.info("业务查单完成,并再次发送业务支付通知.发送结果:{}", s);
        }
        return payOrder;
    }

    public String doWxRefundReq(String tradeType, JSONObject refundOrder, String resKey) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("tradeType", tradeType);
        paramMap.put("refundOrder", refundOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map<String, Object> result = payChannel4AliService.doAliRefundReq(jsonParam);
        String s = RpcUtil.mkRet(result);
        if (s == null) {
            return BaldPayUtil.makeRetData(BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_FAIL, "0111", "调用微信支付失败"), resKey);
        }
        Map<String, Object> map = BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.putAll((Map) result.get("bizResult"));
        return BaldPayUtil.makeRetData(map, resKey);
    }

}
