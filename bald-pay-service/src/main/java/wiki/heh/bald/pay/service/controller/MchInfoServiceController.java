package wiki.heh.bald.pay.service.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wiki.heh.bald.pay.common.util.MyBase64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wiki.heh.bald.pay.service.model.MchInfo;
import wiki.heh.bald.pay.service.service.MchInfoService;

/**
 * 商户信息接口
 *
 * @author heh
 * @version v1.0
 */
@Api(tags = "商户信息接口",hidden = true)
@RestController
public class MchInfoServiceController {

    private final Logger _log = LoggerFactory.getLogger(MchInfoServiceController.class);
    @Autowired
    private MchInfoService mchInfoService;

    @ApiOperation("查询商户信息")
    @GetMapping(value = "/mch_info/select")
    public String selectMchInfo(@RequestParam String mchId) {
        MchInfo mchInfo = mchInfoService.selectMchInfo(mchId);
        JSONObject retObj = new JSONObject();
        retObj.put("code", "0000");
        if (mchInfo == null) {
            retObj.put("code", "0002");
            retObj.put("msg", "数据对象不存在");
            return retObj.toJSONString();
        }
        retObj.put("result", JSON.toJSON(mchInfo));
        _log.info("result:{}", retObj.toJSONString());
        return retObj.toJSONString();
    }
}