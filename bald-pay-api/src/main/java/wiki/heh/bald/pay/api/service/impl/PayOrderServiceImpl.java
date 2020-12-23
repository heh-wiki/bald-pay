package wiki.heh.bald.pay.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wiki.heh.bald.pay.api.entity.po.PayOrder;
import wiki.heh.bald.pay.api.entity.vo.Result;
import wiki.heh.bald.pay.api.exception.PayServiceErrorType;
import wiki.heh.bald.pay.api.exception.ServiceException;
import wiki.heh.bald.pay.api.service.*;
import wiki.heh.bald.pay.common.constant.PayConstant;
import wiki.heh.bald.pay.common.domain.BaseParam;
import wiki.heh.bald.pay.common.enumm.RetEnum;
import wiki.heh.bald.pay.common.util.*;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Service
public class PayOrderServiceImpl extends BaseService implements IPayOrderService {

    private static final Logger _log = LoggerFactory.getLogger(PayOrderServiceImpl.class);

    @Autowired
    private INotifyPayService notifyPayService;

    @Autowired
    private IPayChannel4WxService payChannel4WxService;

    @Autowired
    private IPayChannel4AliService payChannel4AliService;

    public int createPayOrder(JSONObject payOrder) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("payOrder", payOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map<String, Object> result = createPayOrder(jsonParam);
        String s = RpcUtil.mkRet(result);
        if(s == null) return 0;
        return Integer.parseInt(s);
    }

    public JSONObject queryPayOrder(String mchId, String payOrderId, String mchOrderNo, String executeNotify) {
        Map<String,Object> paramMap = new HashMap<>();
        Map<String, Object> result;
        if(StringUtils.isNotBlank(payOrderId)) {
            paramMap.put("mchId", mchId);
            paramMap.put("payOrderId", payOrderId);
            String jsonParam = RpcUtil.createBaseParam(paramMap);
            result = selectPayOrderByMchIdAndPayOrderId(jsonParam);
        }else {
            paramMap.put("mchId", mchId);
            paramMap.put("mchOrderNo", mchOrderNo);
            String jsonParam = RpcUtil.createBaseParam(paramMap);
            result = selectPayOrderByMchIdAndMchOrderNo(jsonParam);
        }
        String s = RpcUtil.mkRet(result);
        if(s == null) return null;
        boolean isNotify = Boolean.parseBoolean(executeNotify);
        JSONObject payOrder = JSONObject.parseObject(s);
        if(isNotify) {
            paramMap = new HashMap<>();
            paramMap.put("payOrderId", payOrderId);
            String jsonParam = RpcUtil.createBaseParam(paramMap);
            result = notifyPayService.sendBizPayNotify(jsonParam);
            s = RpcUtil.mkRet(result);
            _log.info("业务查单完成,并再次发送业务支付通知.发送结果:{}", s);
        }
        return payOrder;
    }

    public Result doWxPayReq(String tradeType, JSONObject payOrder, String resKey) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("tradeType", tradeType);
        paramMap.put("payOrder", payOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map<String, Object> result = payChannel4WxService.doWxPayReq(jsonParam);
        String s = RpcUtil.mkRet(result);
        if(s == null) {
            throw new ServiceException(PayServiceErrorType.RET_BIZ_DATA_NOT_EXISTS);
//            return BaldPayUtil.makeRetData(BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_FAIL, "0111", "调用微信支付失败"), resKey);
        }
        Map<String, Object> map = BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.putAll((Map) result.get("bizResult"));
        //todo 需要修改参照支付宝
        return Result.fail();
//        return BaldPayUtil.makeRetData(map, resKey);
    }

    public Result doAliPayReq(String channelId, JSONObject payOrder, String resKey) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("payOrder", payOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Result result = null;
        switch (channelId) {
            case PayConstant.PAY_CHANNEL_ALIPAY_MOBILE :
                result = payChannel4AliService.doAliPayMobileReq(jsonParam,resKey);
                break;
            case PayConstant.PAY_CHANNEL_ALIPAY_PC :
//                result = payChannel4AliService.doAliPayPcReq(jsonParam);
                break;
            case PayConstant.PAY_CHANNEL_ALIPAY_WAP :
//                result = payChannel4AliService.doAliPayWapReq(jsonParam);
                break;
            case PayConstant.PAY_CHANNEL_ALIPAY_QR :
                result = payChannel4AliService.doAliPayQrReq(jsonParam,resKey);
                break;
            default:
                result = null;
                break;
        }
        return result;
//        String s = RpcUtil.mkRet(result);
//        if(s == null) {
//            return BaldPayUtil.makeRetData(BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_FAIL, "0111", "调用支付宝支付失败"), resKey);
//        }
//        Map<String, Object> map = BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
//        map.putAll((Map) result.get("bizResult"));
//        return BaldPayUtil.makeRetData(map, resKey);
    }

    @Override
    public Map createPayOrder(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("新增支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
        if(payOrderObj == null) {
            _log.warn("新增支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        PayOrder payOrder = BeanConvertUtils.map2Bean(payOrderObj, PayOrder.class);
        if(payOrder == null) {
            _log.warn("新增支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result = super.baseCreatePayOrder(payOrder);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map selectPayOrder(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据支付订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String payOrderId = baseParam.isNullValue("payOrderId") ? null : bizParamMap.get("payOrderId").toString();
        if (ObjectValidUtil.isInvalid(payOrderId)) {
            _log.warn("根据支付订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        PayOrder payOrder = super.baseSelectPayOrder(payOrderId);
        if(payOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(payOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map selectPayOrderByMchIdAndPayOrderId(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据商户号和支付订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String payOrderId = baseParam.isNullValue("payOrderId") ? null : bizParamMap.get("payOrderId").toString();
        if (ObjectValidUtil.isInvalid(mchId, payOrderId)) {
            _log.warn("根据商户号和支付订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        PayOrder payOrder = super.baseSelectPayOrderByMchIdAndPayOrderId(mchId, payOrderId);
        if(payOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(payOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map selectPayOrderByMchIdAndMchOrderNo(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据商户号和商户订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String mchOrderNo = baseParam.isNullValue("mchOrderNo") ? null : bizParamMap.get("mchOrderNo").toString();
        if (ObjectValidUtil.isInvalid(mchId, mchOrderNo)) {
            _log.warn("根据商户号和商户订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        PayOrder payOrder = super.baseSelectPayOrderByMchIdAndMchOrderNo(mchId, mchOrderNo);
        if(payOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(payOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map updateStatus4Ing(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改支付订单状态为支付中失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String payOrderId = baseParam.isNullValue("payOrderId") ? null : bizParamMap.get("payOrderId").toString();
        String channelOrderNo = baseParam.isNullValue("channelOrderNo") ? null : bizParamMap.get("channelOrderNo").toString();
        if (ObjectValidUtil.isInvalid(payOrderId, channelOrderNo)) {
            _log.warn("修改支付订单状态为支付中失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Ing(payOrderId, channelOrderNo);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map updateStatus4Success(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改支付订单状态为支付成功失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String payOrderId = baseParam.isNullValue("payOrderId") ? null : bizParamMap.get("payOrderId").toString();
        if (ObjectValidUtil.isInvalid(payOrderId)) {
            _log.warn("修改支付订单状态为支付成功失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result = super.baseUpdateStatus4Success(payOrderId, null);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map updateStatus4Complete(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改支付订单状态为支付完成失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String payOrderId = baseParam.isNullValue("payOrderId") ? null : bizParamMap.get("payOrderId").toString();
        if (ObjectValidUtil.isInvalid(payOrderId)) {
            _log.warn("修改支付订单状态为支付完成失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Complete(payOrderId);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map updateNotify(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改支付订单通知次数失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String payOrderId = baseParam.isNullValue("payOrderId") ? null : bizParamMap.get("payOrderId").toString();
        Byte count = baseParam.isNullValue("count") ? null : Byte.parseByte(bizParamMap.get("count").toString());
        if (ObjectValidUtil.isInvalid(payOrderId, count)) {
            _log.warn("修改支付订单通知次数失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result = super.baseUpdateNotify(payOrderId, count);
        return RpcUtil.createBizResult(baseParam, result);
    }

}
