package wiki.heh.bald.pay.service.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wiki.heh.bald.pay.common.constant.PayConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wiki.heh.bald.pay.common.util.PayDigestUtil;
import wiki.heh.bald.pay.common.util.BaldPayUtil;
import wiki.heh.bald.pay.service.mapper.MchNotifyMapper;
import wiki.heh.bald.pay.service.model.MchInfo;
import wiki.heh.bald.pay.service.model.MchNotify;
import wiki.heh.bald.pay.service.model.PayOrder;
import wiki.heh.bald.pay.service.service.BaseNotify4MchPay;
import wiki.heh.bald.pay.service.service.MchInfoService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


/**
 * 支付通知处理基类
 *
 * @author hehua
 * @version v1.0
 * @date 2017-07-05
 */
@Component
public class Notify4BasePay {
    private static final Logger _log = LoggerFactory.getLogger(Notify4BasePay.class);
    //	@Autowired
//	private Mq4PayNotify mq4PayNotify;
    @Resource
    private MchNotifyMapper mchNotifyMapper;
    @Autowired
    private MchInfoService mchInfoService;
    @Autowired
    private BaseNotify4MchPay notify4MchPay;

    /**
     * 创建响应URL
     *
     * @param payOrder
     * @param backType 1：前台页面；2：后台接口
     * @return
     */
    public String createNotifyUrl(PayOrder payOrder, String backType) {
        String mchId = payOrder.getMchId();
        MchInfo mchInfo = mchInfoService.selectMchInfo(mchId);
        String resKey = mchInfo.getResKey();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("payOrderId", payOrder.getPayOrderId() == null ? "" : payOrder.getPayOrderId());           // 支付订单号
        paramMap.put("mchId", payOrder.getMchId() == null ? "" : payOrder.getMchId());                        // 商户ID
        paramMap.put("mchOrderNo", payOrder.getMchOrderNo() == null ? "" : payOrder.getMchOrderNo());        // 商户订单号
        paramMap.put("channelId", payOrder.getChannelId() == null ? "" : payOrder.getChannelId());              // 渠道ID
        paramMap.put("amount", payOrder.getAmount() == null ? "" : payOrder.getAmount());                        // 支付金额
        paramMap.put("currency", payOrder.getCurrency() == null ? "" : payOrder.getCurrency());                 // 货币类型
        paramMap.put("status", payOrder.getStatus() == null ? "" : payOrder.getStatus());                    // 支付状态
        paramMap.put("clientIp", payOrder.getClientIp() == null ? "" : payOrder.getClientIp());                // 客户端IP
        paramMap.put("device", payOrder.getDevice() == null ? "" : payOrder.getDevice());                        // 设备
        paramMap.put("subject", payOrder.getSubject() == null ? "" : payOrder.getSubject());                        // 商品标题
        paramMap.put("channelOrderNo", payOrder.getChannelOrderNo() == null ? "" : payOrder.getChannelOrderNo()); // 渠道订单号
        paramMap.put("param1", payOrder.getParam1() == null ? "" : payOrder.getParam1());                        // 扩展参数1
        paramMap.put("param2", payOrder.getParam2() == null ? "" : payOrder.getParam2());                        // 扩展参数2
        paramMap.put("paySuccTime", payOrder.getPaySuccTime() == null ? "" : payOrder.getPaySuccTime());            // 支付成功时间
        paramMap.put("backType", backType == null ? "" : backType);
        // 先对原文签名
        String reqSign = PayDigestUtil.getSign(paramMap, resKey);
        paramMap.put("sign", reqSign);   // 签名
        // 签名后再对有中文参数编码
        try {
            paramMap.put("device", URLEncoder.encode(payOrder.getDevice() == null ? "" : payOrder.getDevice(), PayConstant.RESP_UTF8));
            paramMap.put("subject", URLEncoder.encode(payOrder.getSubject() == null ? "" : payOrder.getSubject(), PayConstant.RESP_UTF8));
            paramMap.put("param1", URLEncoder.encode(payOrder.getParam1() == null ? "" : payOrder.getParam1(), PayConstant.RESP_UTF8));
            paramMap.put("param2", URLEncoder.encode(payOrder.getParam2() == null ? "" : payOrder.getParam2(), PayConstant.RESP_UTF8));
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
     * 处理支付结果前台页面跳转
     */
    public boolean doPage(PayOrder payOrder, HttpServletResponse response) {
        String redirectUrl = createNotifyUrl(payOrder, "1");
        _log.info("redirect to respUrl:" + redirectUrl);
        // 前台跳转业务系统
        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            _log.error("bald-pay sendRedirect exception. respUrl=" + redirectUrl, e);
            return false;
        }
        return true;
    }

    /**
     * 处理支付结果后台服务器通知
     */
    public void doNotify(PayOrder payOrder, boolean isFirst) {
        _log.info(">>>>>> PAY开始回调通知业务系统 <<<<<<");
        // 发起后台通知业务系统
        JSONObject object = createNotifyInfo(payOrder, isFirst);
        try {
            //todo 消息通知
            notify4MchPay.receive1(object.toJSONString());
//			mq4PayNotify.send(object.toJSONString());

        } catch (Exception e) {
            _log.error("payOrderId={},sendMessage error.", payOrder != null ? payOrder.getPayOrderId() : "", e);
        }
        _log.info(">>>>>> PAY回调通知业务系统完成 <<<<<<");
    }

    public JSONObject createNotifyInfo(PayOrder payOrder, boolean isFirst) {
        String url = createNotifyUrl(payOrder, "2");
        int count = 0;
        if (isFirst) {

            MchNotify mchNotify = new MchNotify();
            mchNotify.setOrderId(payOrder.getPayOrderId());
            mchNotify.setMchId(payOrder.getMchId());
            mchNotify.setMchOrderNo(payOrder.getMchOrderNo());
            mchNotify.setOrderType(PayConstant.MCH_NOTIFY_TYPE_PAY);
            mchNotify.setNotifyUrl(url);
            int result = mchNotifyMapper.insertSelectiveOnDuplicateKeyUpdate(mchNotify);
            _log.info("增加商户通知记录,orderId={},result:{}", payOrder.getPayOrderId(), result);
        } else {
            MchNotify mchNotify = mchNotifyMapper.selectByPrimaryKey(payOrder.getPayOrderId());
            if (mchNotify != null) count = mchNotify.getNotifyCount();
        }
        JSONObject object = new JSONObject();
        object.put("method", "GET");
        object.put("url", createNotifyUrl(payOrder, "2"));
        object.put("orderId", payOrder.getPayOrderId());
        object.put("count", count);
        object.put("createTime", System.currentTimeMillis());
        return object;
    }

}
