package wiki.heh.bald.pay.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import wiki.heh.bald.pay.api.entity.po.MchInfo;
import wiki.heh.bald.pay.api.service.BaseService;
import wiki.heh.bald.pay.api.service.IMchInfoService;
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
public class MchInfoServiceImpl extends BaseService implements IMchInfoService {

    private static final Logger _log = LoggerFactory.getLogger(MchInfoServiceImpl.class);

    @Override
    public Map selectMchInfo(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("查询商户信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        if (ObjectValidUtil.isInvalid(mchId)) {
            _log.warn("查询商户信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        MchInfo mchInfo = super.baseSelectMchInfo(mchId);
        if(mchInfo == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(mchInfo);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    public JSONObject getByMchId(String mchId) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("mchId", mchId);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map<String, Object> result = selectMchInfo(jsonParam);
        String s = RpcUtil.mkRet(result);
        if(s==null) return null;
        return JSONObject.parseObject(s);
    }
}
