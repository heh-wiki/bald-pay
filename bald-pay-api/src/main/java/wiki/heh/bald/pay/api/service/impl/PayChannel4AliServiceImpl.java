package wiki.heh.bald.pay.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.request.*;
import com.alipay.api.response.AlipayFundTransOrderQueryResponse;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wiki.heh.bald.pay.api.config.channel.alipay.AlipayConfig;
import wiki.heh.bald.pay.api.entity.po.PayChannel;
import wiki.heh.bald.pay.api.entity.po.PayOrder;
import wiki.heh.bald.pay.api.entity.po.RefundOrder;
import wiki.heh.bald.pay.api.entity.po.TransOrder;
import wiki.heh.bald.pay.api.entity.vo.Result;
import wiki.heh.bald.pay.api.exception.PayServiceErrorType;
import wiki.heh.bald.pay.api.exception.ServiceException;
import wiki.heh.bald.pay.api.service.BaseService;
import wiki.heh.bald.pay.api.service.BaseService4PayOrder;
import wiki.heh.bald.pay.api.service.BaseService4TransOrder;
import wiki.heh.bald.pay.api.service.IPayChannel4AliService;
import wiki.heh.bald.pay.common.constant.PayConstant;
import wiki.heh.bald.pay.common.domain.BaseParam;
import wiki.heh.bald.pay.common.enumm.RetEnum;
import wiki.heh.bald.pay.common.util.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Service
public class PayChannel4AliServiceImpl extends BaseService implements IPayChannel4AliService {

    private static final Logger _log = LoggerFactory.getLogger(PayChannel4AliServiceImpl.class);

    @Autowired
    BaseService4TransOrder baseService4TransOrder;

    @Autowired
    BaseService4PayOrder baseService4PayOrder;

    @Autowired
    private AlipayConfig alipayConfig;

    @Override
    public Map doAliPayWapReq(String jsonParam) {
        String logPrefix = "【支付宝WAP支付下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
        PayOrder payOrder = BeanConvertUtils.map2Bean(payOrderObj, PayOrder.class);
        if (ObjectValidUtil.isInvalid(payOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String payOrderId = payOrder.getPayOrderId();
        String mchId = payOrder.getMchId();
        String channelId = payOrder.getChannelId();
        PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradeWapPayRequest alipay_request = new AlipayTradeWapPayRequest();
        // 封装请求支付信息
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        model.setProductCode("QUICK_WAP_PAY");
        // 获取objParams参数
        String objParams = payOrder.getExtra();
        if (StringUtils.isNotEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSON.parseObject(objParams);
                if (StringUtils.isNotBlank(objParamsJson.getString("quit_url"))) {
                    model.setQuitUrl(objParamsJson.getString("quit_url"));
                }
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getNotify_url());
        // 设置同步地址
        alipay_request.setReturnUrl(alipayConfig.getReturn_url());
        String payUrl = null;
        try {
            payUrl = client.pageExecute(alipay_request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);
        super.baseUpdateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");
        Map<String, Object> map = BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.put("payOrderId", payOrderId);
        map.put("payUrl", payUrl);
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map doAliPayPcReq(String jsonParam) {
        String logPrefix = "【支付宝PC支付下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
        PayOrder payOrder = BeanConvertUtils.map2Bean(payOrderObj, PayOrder.class);
        if (ObjectValidUtil.isInvalid(payOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String payOrderId = payOrder.getPayOrderId();
        String mchId = payOrder.getMchId();
        String channelId = payOrder.getChannelId();
        PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradePagePayRequest alipay_request = new AlipayTradePagePayRequest();
        // 封装请求支付信息
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        // 获取objParams参数
        String objParams = payOrder.getExtra();
        String qr_pay_mode = "2";
        String qrcode_width = "200";
        if (StringUtils.isNotEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSON.parseObject(objParams);
                qr_pay_mode = ObjectUtils.toString(objParamsJson.getString("qr_pay_mode"), "2");
                qrcode_width = ObjectUtils.toString(objParamsJson.getString("qrcode_width"), "200");
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }
        model.setQrPayMode(qr_pay_mode);
        model.setQrcodeWidth(Long.parseLong(qrcode_width));
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getNotify_url());
        // 设置同步地址
        alipay_request.setReturnUrl(alipayConfig.getReturn_url());
        String payUrl = null;
        try {
            payUrl = client.pageExecute(alipay_request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);
        super.baseUpdateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");
        Map<String, Object> map = BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.put("payOrderId", payOrderId);
        map.put("payUrl", payUrl);
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map doAliPayMobileReq(String jsonParam) {
        String logPrefix = "【支付宝APP支付下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
        PayOrder payOrder = BeanConvertUtils.map2Bean(payOrderObj, PayOrder.class);
        if (ObjectValidUtil.isInvalid(payOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String payOrderId = payOrder.getPayOrderId();
        String mchId = payOrder.getMchId();
        String channelId = payOrder.getChannelId();
        PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradeAppPayRequest alipay_request = new AlipayTradeAppPayRequest();
        // 封装请求支付信息
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        model.setProductCode("QUICK_MSECURITY_PAY");
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getNotify_url());
        // 设置同步地址
        alipay_request.setReturnUrl(alipayConfig.getReturn_url());
        String payParams = null;
        try {
            payParams = client.sdkExecute(alipay_request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        super.baseUpdateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,payParams={}", logPrefix, payParams);
        _log.info("###### 商户统一下单处理完成 ######");
        Map<String, Object> map = BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.put("payOrderId", payOrderId);
        map.put("payParams", payParams);
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Result doAliPayQrReq(String jsonParam, String resKey) {
        String logPrefix = "【支付宝当面付之扫码支付下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            throw new ServiceException(PayServiceErrorType.RET_PARAM_NOT_FOUND);
        }
        JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
        PayOrder payOrder = BeanConvertUtils.map2Bean(payOrderObj, PayOrder.class);
        if (ObjectValidUtil.isInvalid(payOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            throw new ServiceException(PayServiceErrorType.RET_PARAM_INVALID);
        }
        String payOrderId = payOrder.getPayOrderId();
        String mchId = payOrder.getMchId();
        String channelId = payOrder.getChannelId();
        PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradePrecreateRequest alipay_request = new AlipayTradePrecreateRequest();
        // 封装请求支付信息
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        // 获取objParams参数
        String objParams = payOrder.getExtra();
        if (StringUtils.isNotEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSON.parseObject(objParams);
                if (StringUtils.isNotBlank(objParamsJson.getString("discountable_amount"))) {
                    //可打折金额
                    model.setDiscountableAmount(objParamsJson.getString("discountable_amount"));
                }
                if (StringUtils.isNotBlank(objParamsJson.getString("undiscountable_amount"))) {
                    //不可打折金额
                    model.setUndiscountableAmount(objParamsJson.getString("undiscountable_amount"));
                }
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getNotify_url());
        // 设置同步地址
        alipay_request.setReturnUrl(alipayConfig.getReturn_url());
        String payUrl = null;
        try {

            payUrl = client.execute(alipay_request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);
        super.baseUpdateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");
//        Map<String, Object> map = BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        Map<String, Object> map = JSON.parseObject(payUrl).getJSONObject("alipay_trade_precreate_response");
        map.remove("msg");
        map.remove("code");
//        map.put("payOrderId", payOrderId);
        return Result.sign(PayDigestUtil.getSign(map, resKey, "payParams"), map);
//        return RpcUtil.createBizResult(baseParam, map);
    }

    /**
     * 支付宝转账,文档:https://docs.open.alipay.com/api_28/alipay.fund.trans.toaccount.transfer
     *
     * @param jsonParam
     * @return
     */
    @Override
    public Map doAliTransReq(String jsonParam) {
        String logPrefix = "【支付宝转账】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject transOrderObj = baseParam.isNullValue("transOrder") ? null : JSONObject.parseObject(bizParamMap.get("transOrder").toString());
        TransOrder transOrder = JSON.toJavaObject(transOrderObj, TransOrder.class);
        if (ObjectValidUtil.isInvalid(transOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String transOrderId = transOrder.getTransOrderId();
        String mchId = transOrder.getMchId();
        String channelId = transOrder.getChannelId();
        PayChannel payChannel = baseService4TransOrder.baseSelectPayChannel(mchId, channelId);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
        AlipayFundTransToaccountTransferModel model = new AlipayFundTransToaccountTransferModel();
        model.setOutBizNo(transOrderId);
        model.setPayeeType("ALIPAY_LOGONID");                            // 收款方账户类型
        model.setPayeeAccount(transOrder.getChannelUser());              // 收款方账户
        model.setAmount(AmountUtil.convertCent2Dollar(transOrder.getAmount().toString()));
        model.setPayerShowName("支付转账");
        model.setPayeeRealName(transOrder.getUserName());
        model.setRemark(transOrder.getRemarkInfo());
        request.setBizModel(model);
        Map<String, Object> map = new HashMap<>();
        map.put("transOrderId", transOrderId);
        map.put("isSuccess", false);
        try {
            AlipayFundTransToaccountTransferResponse response = client.execute(request);
            if (response.isSuccess()) {
                map.put("isSuccess", true);
                map.put("channelOrderNo", response.getOrderId());
            } else {
                //出现业务错误
                _log.info("{}返回失败", logPrefix);
                _log.info("sub_code:{},sub_msg:{}", response.getSubCode(), response.getSubMsg());
                map.put("channelErrCode", response.getSubCode());
                map.put("channelErrMsg", response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            _log.error("");
        }
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map getAliTransReq(String jsonParam) {
        String logPrefix = "【支付宝转账查询】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject transOrderObj = baseParam.isNullValue("transOrder") ? null : JSONObject.parseObject(bizParamMap.get("transOrder").toString());
        TransOrder transOrder = JSON.toJavaObject(transOrderObj, TransOrder.class);
        if (ObjectValidUtil.isInvalid(transOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String transOrderId = transOrder.getTransOrderId();
        String mchId = transOrder.getMchId();
        String channelId = transOrder.getChannelId();
        PayChannel payChannel = baseService4PayOrder.baseSelectPayChannel(mchId, channelId);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayFundTransOrderQueryRequest request = new AlipayFundTransOrderQueryRequest();
        AlipayFundTransOrderQueryModel model = new AlipayFundTransOrderQueryModel();
        model.setOutBizNo(transOrderId);
        model.setOrderId(transOrder.getChannelOrderNo());
        request.setBizModel(model);
        Map<String, Object> map = BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.put("transOrderId", transOrderId);
        try {
            AlipayFundTransOrderQueryResponse response = client.execute(request);
            if (response.isSuccess()) {
                map.putAll((Map) JSON.toJSON(response));
                map.put("isSuccess", true);
            } else {
                _log.info("{}返回失败", logPrefix);
                _log.info("sub_code:{},sub_msg:{}", response.getSubCode(), response.getSubMsg());
                map.put("channelErrCode", response.getSubCode());
                map.put("channelErrMsg", response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            _log.error("");
        }
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map doAliRefundReq(String jsonParam) {
        String logPrefix = "【支付宝退款】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject refundOrderObj = baseParam.isNullValue("refundOrder") ? null : JSONObject.parseObject(bizParamMap.get("refundOrder").toString());
        RefundOrder refundOrder = JSON.toJavaObject(refundOrderObj, RefundOrder.class);
        if (ObjectValidUtil.isInvalid(refundOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String refundOrderId = refundOrder.getRefundOrderId();
        String mchId = refundOrder.getMchId();
        String channelId = refundOrder.getChannelId();
        PayChannel payChannel = baseService4PayOrder.baseSelectPayChannel(mchId, channelId);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(refundOrder.getPayOrderId());
        model.setTradeNo(refundOrder.getChannelPayOrderNo());
        model.setOutRequestNo(refundOrderId);
        model.setRefundAmount(AmountUtil.convertCent2Dollar(refundOrder.getRefundAmount().toString()));
        model.setRefundReason("正常退款");
        request.setBizModel(model);
        Map<String, Object> map = new HashMap<>();
        map.put("refundOrderId", refundOrderId);
        map.put("isSuccess", false);
        try {
            AlipayTradeRefundResponse response = client.execute(request);
            if (response.isSuccess()) {
                map.put("isSuccess", true);
                map.put("channelOrderNo", response.getTradeNo());
            } else {
                _log.info("{}返回失败", logPrefix);
                _log.info("sub_code:{},sub_msg:{}", response.getSubCode(), response.getSubMsg());
                map.put("channelErrCode", response.getSubCode());
                map.put("channelErrMsg", response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            _log.error("");
        }
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map getAliRefundReq(String jsonParam) {
        String logPrefix = "【支付宝退款查询】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject refundOrderObj = baseParam.isNullValue("refundOrder") ? null : JSONObject.parseObject(bizParamMap.get("refundOrder").toString());
        RefundOrder refundOrder = JSON.toJavaObject(refundOrderObj, RefundOrder.class);
        if (ObjectValidUtil.isInvalid(refundOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String refundOrderId = refundOrder.getRefundOrderId();
        String mchId = refundOrder.getMchId();
        String channelId = refundOrder.getChannelId();
        PayChannel payChannel = baseService4PayOrder.baseSelectPayChannel(mchId, channelId);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setOutTradeNo(refundOrder.getPayOrderId());
        model.setTradeNo(refundOrder.getChannelPayOrderNo());
        model.setOutRequestNo(refundOrderId);
        request.setBizModel(model);
        Map<String, Object> map = new HashMap<>();
        map.put("refundOrderId", refundOrderId);
        try {
            AlipayTradeFastpayRefundQueryResponse response = client.execute(request);
            if (response.isSuccess()) {
                map.putAll((Map) JSON.toJSON(response));
                map.put("isSuccess", true);
            } else {
                _log.info("{}返回失败", logPrefix);
                _log.info("sub_code:{},sub_msg:{}", response.getSubCode(), response.getSubMsg());
                map.put("channelErrCode", response.getSubCode());
                map.put("channelErrMsg", response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            _log.error("");
        }
        return RpcUtil.createBizResult(baseParam, map);
    }
}
