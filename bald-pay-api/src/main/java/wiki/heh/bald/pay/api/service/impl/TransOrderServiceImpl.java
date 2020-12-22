package wiki.heh.bald.pay.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wiki.heh.bald.pay.api.entity.po.TransOrder;
import wiki.heh.bald.pay.api.exception.PayServiceErrorType;
import wiki.heh.bald.pay.api.exception.ServiceException;
import wiki.heh.bald.pay.api.mq.Mq4TransNotify;
import wiki.heh.bald.pay.api.service.BaseService4TransOrder;
import wiki.heh.bald.pay.api.service.ITransOrderService;
import wiki.heh.bald.pay.common.domain.BaseParam;
import wiki.heh.bald.pay.common.enumm.RetEnum;
import wiki.heh.bald.pay.common.util.BeanConvertUtils;
import wiki.heh.bald.pay.common.util.JsonUtil;
import wiki.heh.bald.pay.common.util.ObjectValidUtil;
import wiki.heh.bald.pay.common.util.RpcUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Service
public class TransOrderServiceImpl extends BaseService4TransOrder implements ITransOrderService {

    private static final Logger _log = LoggerFactory.getLogger(TransOrderServiceImpl.class);

    @Autowired
    private Mq4TransNotify mq4TransNotify;

    @Override
    public int create(JSONObject transOrder1) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("transOrder", transOrder1);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("新增转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            throw new ServiceException(PayServiceErrorType.RET_PARAM_NOT_FOUND);
        }
        JSONObject transOrderObj = baseParam.isNullValue("transOrder") ? null : JSONObject.parseObject(bizParamMap.get("transOrder").toString());
        if (transOrderObj == null) {
            _log.warn("新增转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            throw new ServiceException(PayServiceErrorType.RET_PARAM_INVALID);
        }
        TransOrder transOrder = BeanConvertUtils.map2Bean(transOrderObj, TransOrder.class);
        if (transOrder == null) {
            _log.warn("新增转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            throw new ServiceException(PayServiceErrorType.RET_PARAM_INVALID);
        }
        int result = super.baseCreateTransOrder(transOrder);
        return result;
//        return RpcUtil.createBizResult(baseParam, result);
//        String s = RpcUtil.mkRet(RpcUtil.createBizResult(baseParam, result));
//        if(s == null) return 0;
//        return Integer.parseInt(s);
    }

    @Override
    public Map select(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据转账订单号查询转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        if (ObjectValidUtil.isInvalid(transOrderId)) {
            _log.warn("根据转账订单号查询转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        TransOrder transOrder = super.baseSelectTransOrder(transOrderId);
        if (transOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(transOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map selectByMchIdAndTransOrderId(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据商户号和转账订单号查询转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        if (ObjectValidUtil.isInvalid(mchId, transOrderId)) {
            _log.warn("根据商户号和转账订单号查询转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        TransOrder transOrder = super.baseSelectByMchIdAndTransOrderId(mchId, transOrderId);
        if (transOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(transOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map selectByMchIdAndMchTransNo(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据商户号和商户订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String mchTransNo = baseParam.isNullValue("mchTransNo") ? null : bizParamMap.get("mchTransNo").toString();
        if (ObjectValidUtil.isInvalid(mchId, mchTransNo)) {
            _log.warn("根据商户号和商户订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        TransOrder transOrder = super.baseSelectByMchIdAndMchTransNo(mchId, mchTransNo);
        if (transOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(transOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map updateStatus4Ing(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        String channelOrderNo = baseParam.isNullValue("channelOrderNo") ? null : bizParamMap.get("channelOrderNo").toString();
        if (ObjectValidUtil.isInvalid(transOrderId)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result = super.baseUpdateStatus4Ing(transOrderId, channelOrderNo);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map updateStatus4Success(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        if (ObjectValidUtil.isInvalid(transOrderId)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result = super.baseUpdateStatus4Success(transOrderId);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map updateStatus4Complete(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        if (ObjectValidUtil.isInvalid(transOrderId)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result = super.baseUpdateStatus4Complete(transOrderId);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map sendTransNotify(String transOrderId, String channelName)  {
        JSONObject object = new JSONObject();
        object.put("transOrderId", transOrderId);
        object.put("channelName", channelName);
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("msg", object);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("发送转账订单处理失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String msg = baseParam.isNullValue("msg") ? null : bizParamMap.get("msg").toString();
        if (ObjectValidUtil.isInvalid(msg)) {
            _log.warn("发送转账订单处理失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result = 1;
        try {
            mq4TransNotify.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
            _log.error("");
            result = 0;
        }
        return RpcUtil.createBizResult(baseParam, result);
    }
}
