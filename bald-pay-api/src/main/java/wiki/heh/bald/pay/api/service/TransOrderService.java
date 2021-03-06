//package wiki.heh.bald.pay.api.service;
//
//import com.alibaba.fastjson.JSONObject;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// *
// * @author heh
// * @version v1.0
// * @date 2020-12-18
// */
//@Service
//public class TransOrderService {
//
//    private static final Logger _log = LoggerFactory.getLogger(TransOrderService.class);
//
//    @Autowired
//    private RpcCommonService rpcCommonService;
//
//    public int create(JSONObject transOrder) {
//        Map<String,Object> paramMap = new HashMap<>();
//        paramMap.put("transOrder", transOrder);
//        String jsonParam = RpcUtil.createBaseParam(paramMap);
//        Map<String, Object> result = rpcCommonService.rpcTransOrderService.create(jsonParam);
//        String s = RpcUtil.mkRet(result);
//        if(s == null) return 0;
//        return Integer.parseInt(s);
//    }
//
//    public void sendTransNotify(String transOrderId, String channelName) {
//        JSONObject object = new JSONObject();
//        object.put("transOrderId", transOrderId);
//        object.put("channelName", channelName);
//        Map<String,Object> paramMap = new HashMap<>();
//        paramMap.put("msg", object);
//        String jsonParam = RpcUtil.createBaseParam(paramMap);
//        rpcCommonService.rpcTransOrderService.sendTransNotify(jsonParam);
//    }
//
//    public JSONObject query(String mchId, String transOrderId, String mchTransNo, String executeNotify) {
//        Map<String,Object> paramMap = new HashMap<>();
//        Map<String, Object> result;
//        if(StringUtils.isNotBlank(transOrderId)) {
//            paramMap.put("mchId", mchId);
//            paramMap.put("transOrderId", transOrderId);
//            String jsonParam = RpcUtil.createBaseParam(paramMap);
//            result = rpcCommonService.rpcTransOrderService.selectByMchIdAndTransOrderId(jsonParam);
//        }else {
//            paramMap.put("mchId", mchId);
//            paramMap.put("mchTransNo", mchTransNo);
//            String jsonParam = RpcUtil.createBaseParam(paramMap);
//            result = rpcCommonService.rpcTransOrderService.selectByMchIdAndMchTransNo(jsonParam);
//        }
//        String s = RpcUtil.mkRet(result);
//        if(s == null) return null;
//        boolean isNotify = Boolean.parseBoolean(executeNotify);
//        JSONObject payOrder = JSONObject.parseObject(s);
//        if(isNotify) {
//            paramMap = new HashMap<>();
//            paramMap.put("transOrderId", transOrderId);
//            String jsonParam = RpcUtil.createBaseParam(paramMap);
//            result = rpcCommonService.rpcNotifyPayService.sendBizPayNotify(jsonParam);
//            s = RpcUtil.mkRet(result);
//            _log.info("业务查单完成,并再次发送业务支付通知.发送结果:{}", s);
//        }
//        return payOrder;
//    }
//
//    public String doWxTransReq(String tradeType, JSONObject payOrder, String resKey) {
//        Map<String,Object> paramMap = new HashMap<>();
//        paramMap.put("tradeType", tradeType);
//        paramMap.put("payOrder", payOrder);
//        String jsonParam = RpcUtil.createBaseParam(paramMap);
//        Map<String, Object> result = rpcCommonService.rpcPayChannel4WxService.doWxPayReq(jsonParam);
//        String s = RpcUtil.mkRet(result);
//        if(s == null) {
//            return BaldPayUtil.makeRetData(BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_FAIL, "0111", "调用微信支付失败"), resKey);
//        }
//        Map<String, Object> map = BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
//        map.putAll((Map) result.get("bizResult"));
//        return BaldPayUtil.makeRetData(map, resKey);
//    }
//
//
//}
