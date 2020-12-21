package wiki.heh.bald.pay.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import wiki.heh.bald.pay.api.entity.po.PayChannel;
import wiki.heh.bald.pay.api.service.BaseService;
import wiki.heh.bald.pay.api.service.IPayChannelService;
import wiki.heh.bald.pay.common.domain.BaseParam;
import wiki.heh.bald.pay.common.enumm.RetEnum;
import wiki.heh.bald.pay.common.util.JsonUtil;
import wiki.heh.bald.pay.common.util.ObjectValidUtil;
import wiki.heh.bald.pay.common.util.RpcUtil;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Service
public class PayChannelServiceImpl extends BaseService implements IPayChannelService {

    private static final Logger _log = LoggerFactory.getLogger(PayChannelServiceImpl.class);

    @Override
    public Map selectPayChannel(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("查询支付渠道信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String channelId = baseParam.isNullValue("channelId") ? null : bizParamMap.get("channelId").toString();
        if (ObjectValidUtil.isInvalid(mchId, channelId)) {
            _log.warn("查询支付渠道信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
        if(payChannel == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(payChannel);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    public JSONObject getByMchIdAndChannelId(String mchId, String channelId) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("mchId", mchId);
        paramMap.put("channelId", channelId);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map<String, Object> result = selectPayChannel(jsonParam);
        String s = RpcUtil.mkRet(result);
        if(s == null) return null;
        return JSONObject.parseObject(s);
    }
}
