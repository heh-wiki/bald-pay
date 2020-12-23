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
import wiki.heh.bald.pay.service.model.PayChannel;
import wiki.heh.bald.pay.service.service.PayChannelService;

/**
 * 支付渠道接口
 *
 * @author heh
 * @version v1.0
  * @date 2020-07-05
 */
@Api(tags = "支付渠道接口")
@RestController
public class PayChannelServiceController {
    private final Logger _log = LoggerFactory.getLogger(PayChannelServiceController.class);
    @Autowired
    private PayChannelService payChannelService;

    @ApiOperation("查询商户对应的支付渠道")
    @GetMapping("pay_channel/select")
    public String selectPayChannel(String channelId, String mchId) {
        _log.info("channelId << {},mchId<<{}", channelId, mchId);
        JSONObject retObj = new JSONObject();
        retObj.put("code", "0000");
        PayChannel payChannel = payChannelService.selectPayChannel(channelId, mchId);
        if (payChannel == null) {
            retObj.put("code", "0002");
            retObj.put("msg", "数据对象不存在");
            return retObj.toJSONString();
        }
        retObj.put("result", JSON.toJSON(payChannel));
        _log.info("selectPayChannel >> {}", retObj);
        return retObj.toJSONString();
    }
}
