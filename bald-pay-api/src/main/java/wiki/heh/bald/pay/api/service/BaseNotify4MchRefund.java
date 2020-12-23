package wiki.heh.bald.pay.api.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wiki.heh.bald.pay.api.entity.po.MchInfo;
import wiki.heh.bald.pay.api.entity.po.MchNotify;
import wiki.heh.bald.pay.api.entity.po.RefundOrder;
import wiki.heh.bald.pay.api.mq.Mq4MchRefundNotify;
import wiki.heh.bald.pay.common.constant.PayConstant;
import wiki.heh.bald.pay.common.util.BaldPayUtil;
import wiki.heh.bald.pay.common.util.PayDigestUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 *  商户转账通知处理基类
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Component
public class BaseNotify4MchRefund extends BaseService4RefundOrder {

	private static final Logger _log = LoggerFactory.getLogger(BaseNotify4MchRefund.class);

	@Autowired
	private Mq4MchRefundNotify mq4MchRefundNotify;

	/**
	 * 创建响应URL
	 * @param refundOrder
	 * @param backType 1：前台页面；2：后台接口
	 * @return
	 */
	public String createNotifyUrl(RefundOrder refundOrder, String backType) {
		String mchId = refundOrder.getMchId();
		MchInfo mchInfo = super.baseSelectMchInfo(mchId);
		String resKey = mchInfo.getResKey();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("refundOrderId", ObjectUtils.defaultIfNull(refundOrder.getRefundOrderId(), ""));           // 退款订单号
		paramMap.put("mchId", ObjectUtils.defaultIfNull(refundOrder.getMchId(), ""));                      	 	// 商户ID
		paramMap.put("mchOrderNo", ObjectUtils.defaultIfNull(refundOrder.getMchRefundNo(), ""));       		 	// 商户订单号
		paramMap.put("channelId", ObjectUtils.defaultIfNull(refundOrder.getChannelId(), ""));              		// 渠道ID
		paramMap.put("refundAmount", ObjectUtils.defaultIfNull(refundOrder.getRefundAmount(), ""));             // 退款金额
		paramMap.put("currency", ObjectUtils.defaultIfNull(refundOrder.getCurrency(), ""));                 	// 货币类型
		paramMap.put("status", ObjectUtils.defaultIfNull(refundOrder.getStatus(), ""));               			// 退款状态
		paramMap.put("result", ObjectUtils.defaultIfNull(refundOrder.getResult(), ""));               			// 退款结果
		paramMap.put("clientIp", ObjectUtils.defaultIfNull(refundOrder.getClientIp(), ""));   					// 客户端IP
		paramMap.put("device", ObjectUtils.defaultIfNull(refundOrder.getDevice(), ""));               			// 设备
		paramMap.put("channelOrderNo", ObjectUtils.defaultIfNull(refundOrder.getChannelOrderNo(), "")); 		// 渠道订单号
		paramMap.put("param1", ObjectUtils.defaultIfNull(refundOrder.getParam1(), ""));               		   	// 扩展参数1
		paramMap.put("param2", ObjectUtils.defaultIfNull(refundOrder.getParam2(), ""));               		   	// 扩展参数2
		paramMap.put("refundSuccTime", ObjectUtils.defaultIfNull(refundOrder.getRefundSuccTime(), ""));			// 退款成功时间
		paramMap.put("backType", backType==null ? "" : backType);
		// 先对原文签名
		String reqSign = PayDigestUtil.getSign(paramMap, resKey);
		paramMap.put("sign", reqSign);   // 签名
		// 签名后再对有中文参数编码
		try {
			paramMap.put("device", URLEncoder.encode(ObjectUtils.defaultIfNull(refundOrder.getDevice(), ""), PayConstant.RESP_UTF8));
			paramMap.put("param1", URLEncoder.encode(ObjectUtils.defaultIfNull(refundOrder.getParam1(), ""), PayConstant.RESP_UTF8));
			paramMap.put("param2", URLEncoder.encode(ObjectUtils.defaultIfNull(refundOrder.getParam2(), ""), PayConstant.RESP_UTF8));
		}catch (UnsupportedEncodingException e) {
			_log.error("URL Encode exception.", e);
			return null;
		}
		String param = BaldPayUtil.genUrlParams(paramMap);
		StringBuffer sb = new StringBuffer();
		sb.append(refundOrder.getNotifyUrl()).append("?").append(param);
		return sb.toString();
	}

	/**
	 * 处理商户转账后台服务器通知
	 */
	public void doNotify(RefundOrder refundOrder, boolean isFirst) {
		_log.info(">>>>>> REFUND开始回调通知业务系统 <<<<<<");
		// 发起后台通知业务系统
		JSONObject object = createNotifyInfo(refundOrder, isFirst);
		try {
			mq4MchRefundNotify.send(object.toJSONString());
		} catch (Exception e) {
			_log.error("refundOrderId=%s,sendMessage error.", ObjectUtils.defaultIfNull(refundOrder.getRefundOrderId(), ""));
		}
		_log.info(">>>>>> REFUND回调通知业务系统完成 <<<<<<");
	}

	public JSONObject createNotifyInfo(RefundOrder refundOrder, boolean isFirst) {
		String url = createNotifyUrl(refundOrder, "2");
		if(isFirst) {
			int result = baseInsertMchNotify(refundOrder.getRefundOrderId(), refundOrder.getMchId(), refundOrder.getMchRefundNo(), PayConstant.MCH_NOTIFY_TYPE_REFUND, url);
			_log.info("增加商户通知记录,orderId={},result:{}", refundOrder.getRefundOrderId(), result);
		}
		int count = 0;
		if(!isFirst) {
			MchNotify mchNotify = baseSelectMchNotify(refundOrder.getRefundOrderId());
			if(mchNotify != null) count = mchNotify.getNotifyCount();
		}
		JSONObject object = new JSONObject();
		object.put("method", "GET");
		object.put("url", url);
		object.put("orderId", refundOrder.getRefundOrderId());
		object.put("count", count);
		object.put("createTime", System.currentTimeMillis());
		return object;
	}

}
