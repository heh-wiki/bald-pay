package wiki.heh.bald.pay.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wiki.heh.bald.pay.api.entity.form.UnifiedRefundForm;
import wiki.heh.bald.pay.api.service.IMchInfoService;
import wiki.heh.bald.pay.api.service.IPayChannelService;
import wiki.heh.bald.pay.api.service.IPayOrderService;
import wiki.heh.bald.pay.api.service.impl.RefundOrderService;
import wiki.heh.bald.pay.common.constant.PayConstant;
import wiki.heh.bald.pay.common.util.BaldPayUtil;
import wiki.heh.bald.pay.common.util.MySeq;

import java.util.Map;

/**
 * 统一退款接口:
 * 1)先验证接口参数以及签名信息
 * 2)验证通过创建支付订单
 * 3)根据商户选择渠道,调用支付服务进行下单
 * 4)返回下单数据
 *
 * @author hehua
 * @version v1.0
 * @date 2020/12/18
 */
@Api(tags = "统一退款接口")
@RestController
public class RefundOrderController {

    private final Logger _log = LoggerFactory.getLogger(RefundOrderController.class);
    @Autowired
    private RefundOrderService refundOrderService;
    @Autowired
    private IPayOrderService payOrderService;
    @Autowired
    private IPayChannelService payChannelService;
    @Autowired
    private IMchInfoService mchInfoService;

    @ApiOperation("创建退款订单")
    @PostMapping("/api/refund/create")
    public String payOrder(@RequestBody UnifiedRefundForm form) {
        _log.info("###### 开始接收商户统一退款请求 ######");
        String logPrefix = "【商户统一退款】";
        try {
            JSONObject po = JSONObject.parseObject(JSON.toJSONString(form));
            JSONObject refundContext = new JSONObject();
            JSONObject refundOrder = null;
            // 验证参数有效性
            Object object = validateParams(po, refundContext);
            if (object instanceof String) {
                _log.info("{}参数校验不通过:{}", logPrefix, object);
                return BaldPayUtil.makeRetFail(BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, object.toString(), null, null));
            }
            if (object instanceof JSONObject) refundOrder = (JSONObject) object;
            if (refundOrder == null)
                return BaldPayUtil.makeRetFail(BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付中心退款失败", null, null));
            int result = refundOrderService.create(refundOrder);
            _log.info("{}创建退款订单,结果:{}", logPrefix, result);
            if (result != 1) {
                return BaldPayUtil.makeRetFail(BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "创建退款订单失败", null, null));
            }
            // 发送异步退款消息
            String transOrderId = refundOrder.getString("refundOrderId");
            String channelName = refundContext.getString("channelName");
            refundOrderService.sendRefundNotify(transOrderId, channelName);
            _log.info("{}发送转账任务完成,transOrderId={}", logPrefix, transOrderId);

            Map<String, Object> map = BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
            map.put("refundOrderId", refundOrder.getString("refundOrderId"));
            return BaldPayUtil.makeRetData(map, refundContext.getString("resKey"));
        } catch (Exception e) {
            e.printStackTrace();
            _log.error(e.getLocalizedMessage());
            return BaldPayUtil.makeRetFail(BaldPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付中心系统异常", null, null));
        }
    }

    /**
     * 验证创建订单请求参数,参数通过返回JSONObject对象,否则返回错误文本信息
     *
     * @param params
     * @return
     */
    private Object validateParams(JSONObject params, JSONObject refundContext) {
        // 验证请求参数,参数有问题返回错误提示
        String errorMessage;
        // 支付参数
        String mchId = params.getString("mchId");                // 商户ID
        String payOrderId = params.getString("payOrderId");     // 支付订单号
        String mchOrderNo = params.getString("mchOrderNo");     // 商户支付单号
        String mchRefundNo = params.getString("mchRefundNo");    // 商户退款单号
        String channelId = params.getString("channelId");        // 渠道ID
        String amount = params.getString("amount");            // 退款金额（单位分）
        String currency = params.getString("currency");         // 币种
        String clientIp = params.getString("clientIp");            // 客户端IP
        String device = params.getString("device");            // 设备
        String extra = params.getString("extra");                // 特定渠道发起时额外参数
        String param1 = params.getString("param1");            // 扩展参数1
        String param2 = params.getString("param2");            // 扩展参数2
        String notifyUrl = params.getString("notifyUrl");        // 退款结果回调URL
        String sign = params.getString("sign");                // 签名
        String channelUser = params.getString("channelUser");    // 渠道用户标识,如微信openId,支付宝账号
        String userName = params.getString("userName");            // 用户姓名
        String remarkInfo = params.getString("remarkInfo");        // 备注
        // 验证请求参数有效性（必选项）
        if (StringUtils.isBlank(mchId)) {
            errorMessage = "request params[mchId] error.";
            return errorMessage;
        }
        if (StringUtils.isBlank(payOrderId) && StringUtils.isBlank(mchOrderNo)) {
            errorMessage = "request params[payOrderId,mchOrderNo] error.";
            return errorMessage;
        }
        if (StringUtils.isBlank(mchRefundNo)) {
            errorMessage = "request params[mchRefundNo] error.";
            return errorMessage;
        }
        if (StringUtils.isBlank(channelId)) {
            errorMessage = "request params[channelId] error.";
            return errorMessage;
        }
        if (!NumberUtils.isNumber(amount)) {
            errorMessage = "request params[amount] error.";
            return errorMessage;
        }
        if (StringUtils.isBlank(currency)) {
            errorMessage = "request params[currency] error.";
            return errorMessage;
        }
        if (StringUtils.isBlank(notifyUrl)) {
            errorMessage = "request params[notifyUrl] error.";
            return errorMessage;
        }
        if (StringUtils.isBlank(channelUser)) {
            errorMessage = "request params[channelUser] error.";
            return errorMessage;
        }

        // 签名信息
//        if (StringUtils.isEmpty(sign)) {
//            errorMessage = "request params[sign] error.";
//            return errorMessage;
//        }

        // 查询商户信息
        JSONObject mchInfo = mchInfoService.getByMchId(mchId);
        if (mchInfo == null) {
            errorMessage = "Can't found mchInfo[mchId=" + mchId + "] record in db.";
            return errorMessage;
        }
        if (mchInfo.getByte("state") != 1) {
            errorMessage = "mchInfo not available [mchId=" + mchId + "] record in db.";
            return errorMessage;
        }

        String reqKey = mchInfo.getString("reqKey");
        if (StringUtils.isBlank(reqKey)) {
            errorMessage = "reqKey is null[mchId=" + mchId + "] record in db.";
            return errorMessage;
        }
        refundContext.put("resKey", mchInfo.getString("resKey"));

        // 查询商户对应的支付渠道
        JSONObject payChannel = payChannelService.getByMchIdAndChannelId(mchId, channelId);
        if (payChannel == null) {
            errorMessage = "Can't found payChannel[channelId=" + channelId + ",mchId=" + mchId + "] record in db.";
            return errorMessage;
        }
        if (payChannel.getByte("state") != 1) {
            errorMessage = "channel not available [channelId=" + channelId + ",mchId=" + mchId + "]";
            return errorMessage;
        }
        refundContext.put("channelName", payChannel.getString("channelName"));

        // 验证签名数据
//        boolean verifyFlag = BaldPayUtil.verifyPaySign(params, reqKey);
//        if (!verifyFlag) {
//            errorMessage = "Verify bald refund sign failed.";
//            return errorMessage;
//        }

        // 验证支付订单是否存在
        JSONObject payOrder = payOrderService.queryPayOrder(mchId, payOrderId, mchOrderNo, "false");
        if (payOrder == null) {
            errorMessage = "payOrder is not exist.";
            return errorMessage;
        }

        String channelPayOrderNo = payOrder.getString("channelOrderNo");    // 渠道测支付单号
        Long payAmount = payOrder.getLong("amount");

        // 验证参数通过,返回JSONObject对象
        JSONObject refundOrder = new JSONObject();
        refundOrder.put("refundOrderId", MySeq.getRefund());
        refundOrder.put("payOrderId", payOrderId);
        refundOrder.put("channelPayOrderNo", channelPayOrderNo);
        refundOrder.put("mchId", mchId);
        refundOrder.put("mchRefundNo", mchRefundNo);
        refundOrder.put("channelId", channelId);
        refundOrder.put("refundAmount", Long.parseLong(amount));    // 退款金额
        refundOrder.put("payAmount", payAmount);                    // 退款金额
        refundOrder.put("currency", currency);
        refundOrder.put("clientIp", clientIp);
        refundOrder.put("device", device);
        refundOrder.put("channelUser", channelUser);
        refundOrder.put("userName", userName);
        refundOrder.put("remarkInfo", remarkInfo);
        refundOrder.put("extra", extra);
        refundOrder.put("channelMchId", payChannel.getString("channelMchId"));
        refundOrder.put("param1", param1);
        refundOrder.put("param2", param2);
        refundOrder.put("notifyUrl", notifyUrl);
        return refundOrder;
    }

}

