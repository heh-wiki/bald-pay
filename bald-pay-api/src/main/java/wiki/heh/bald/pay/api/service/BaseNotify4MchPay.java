package wiki.heh.bald.pay.api.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wiki.heh.bald.pay.api.entity.po.MchInfo;
import wiki.heh.bald.pay.api.entity.po.MchNotify;
import wiki.heh.bald.pay.api.entity.po.PayOrder;
import wiki.heh.bald.pay.api.mq.Mq4MchPayNotify;
import wiki.heh.bald.pay.common.constant.PayConstant;
import wiki.heh.bald.pay.common.util.BaldPayUtil;
import wiki.heh.bald.pay.common.util.PayDigestUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Component
public class BaseNotify4MchPay extends BaseService4PayOrder {

    private static final Logger _log = LoggerFactory.getLogger(BaseNotify4MchPay.class);

    @Autowired
    private Mq4MchPayNotify mq4MchPayNotify;

    /**
     * 创建响应URL
     *
     * @param payOrder
     * @param backType 1：前台页面；2：后台接口
     * @return
     */
    public String createNotifyUrl(PayOrder payOrder, String backType) {
        String mchId = payOrder.getMchId();
        MchInfo mchInfo = super.baseSelectMchInfo(mchId);
        String resKey = mchInfo.getResKey();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("payOrderId", ObjectUtils.defaultIfNull(payOrder.getPayOrderId(), ""));            // 支付订单号
        paramMap.put("mchId", ObjectUtils.defaultIfNull(payOrder.getMchId(), ""));                        // 商户ID
        paramMap.put("mchOrderNo", ObjectUtils.defaultIfNull(payOrder.getMchOrderNo(), ""));            // 商户订单号
        paramMap.put("channelId", ObjectUtils.defaultIfNull(payOrder.getChannelId(), ""));                // 渠道ID
        paramMap.put("amount", ObjectUtils.defaultIfNull(payOrder.getAmount(), ""));                    // 支付金额
        paramMap.put("currency", ObjectUtils.defaultIfNull(payOrder.getCurrency(), ""));                // 货币类型
        paramMap.put("status", ObjectUtils.defaultIfNull(payOrder.getStatus(), ""));                    // 支付状态
        paramMap.put("clientIp", ObjectUtils.defaultIfNull(payOrder.getClientIp(), ""));                // 客户端IP
        paramMap.put("device", ObjectUtils.defaultIfNull(payOrder.getDevice(), ""));                    // 设备
        paramMap.put("subject", ObjectUtils.defaultIfNull(payOrder.getSubject(), ""));                    // 商品标题
        paramMap.put("channelOrderNo", ObjectUtils.defaultIfNull(payOrder.getChannelOrderNo(), ""));    // 渠道订单号
        paramMap.put("param1", ObjectUtils.defaultIfNull(payOrder.getParam1(), ""));                    // 扩展参数1
        paramMap.put("param2", ObjectUtils.defaultIfNull(payOrder.getParam2(), ""));                    // 扩展参数2
        paramMap.put("paySuccTime", ObjectUtils.defaultIfNull(payOrder.getPaySuccTime(), ""));            // 支付成功时间
        paramMap.put("backType", ObjectUtils.defaultIfNull(backType, ""));
        // 先对原文签名
        String reqSign = PayDigestUtil.getSign(paramMap, resKey);
        paramMap.put("sign", reqSign);   // 签名
        // 签名后再对有中文参数编码
        try {
            paramMap.put("device", URLEncoder.encode(ObjectUtils.defaultIfNull(payOrder.getDevice(), ""), PayConstant.RESP_UTF8));
            paramMap.put("subject", URLEncoder.encode(ObjectUtils.defaultIfNull(payOrder.getSubject(), ""), PayConstant.RESP_UTF8));
            paramMap.put("param1", URLEncoder.encode(ObjectUtils.defaultIfNull(payOrder.getParam1(), ""), PayConstant.RESP_UTF8));
            paramMap.put("param2", URLEncoder.encode(ObjectUtils.defaultIfNull(payOrder.getParam2(), ""), PayConstant.RESP_UTF8));
        } catch (UnsupportedEncodingException e) {
            _log.error("URL Encode exception.", e);
            return null;
        }
        String param = BaldPayUtil.genUrlParams(paramMap);
        StringBuffer sb = new StringBuffer();
        sb.append(payOrder.getNotifyUrl()).append("?").append(param);
        return sb.toString();
    }

    /**
     * 处理支付结果后台服务器通知
     */
    public void doNotify(PayOrder payOrder, boolean isFirst) {
        _log.info(">>>>>> PAY开始回调通知业务系统 <<<<<<");
        // 发起后台通知业务系统
        JSONObject object = createNotifyInfo(payOrder, isFirst);
        try {
            mq4MchPayNotify.send(object.toJSONString());
        } catch (Exception e) {
            _log.error("payOrderId={},sendMessage error.", ObjectUtils.defaultIfNull(payOrder.getPayOrderId(), ""));
        }
        _log.info(">>>>>> PAY回调通知业务系统完成 <<<<<<");
    }

    public JSONObject createNotifyInfo(PayOrder payOrder, boolean isFirst) {
        String url = createNotifyUrl(payOrder, "2");
        if (isFirst) {
            int result = baseInsertMchNotify(payOrder.getPayOrderId(), payOrder.getMchId(), payOrder.getMchOrderNo(), PayConstant.MCH_NOTIFY_TYPE_PAY, url);
            _log.info("增加商户通知记录,orderId={},result:{}", payOrder.getPayOrderId(), result);
        }
        int count = 0;
        if (!isFirst) {
            MchNotify mchNotify = baseSelectMchNotify(payOrder.getPayOrderId());
            if (mchNotify != null) count = mchNotify.getNotifyCount();
        }
        JSONObject object = new JSONObject();
        object.put("method", "GET");
        object.put("url", url);
        object.put("orderId", payOrder.getPayOrderId());
        object.put("count", count);
        object.put("createTime", System.currentTimeMillis());
        return object;
    }

}
