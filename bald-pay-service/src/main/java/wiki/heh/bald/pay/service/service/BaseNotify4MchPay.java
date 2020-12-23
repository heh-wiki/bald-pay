package wiki.heh.bald.pay.service.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import wiki.heh.bald.pay.common.constant.PayConstant;
import wiki.heh.bald.pay.common.util.BaldPayUtil;
import wiki.heh.bald.pay.common.util.HttpClient;
import wiki.heh.bald.pay.common.util.PayDigestUtil;
import wiki.heh.bald.pay.service.mapper.MchInfoMapper;
import wiki.heh.bald.pay.service.mapper.MchNotifyMapper;
import wiki.heh.bald.pay.service.mapper.PayOrderMapper;
import wiki.heh.bald.pay.service.model.*;
import wiki.heh.bald.pay.service.model.vo.Result;

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author dingzhiwei jmdhappy@126.com
 * @version V1.0
 * @Description: 商户支付通知处理基类
  * @date 2020-11-01
 * @Copyright: www.xxpay.org
 */
@Service
public class BaseNotify4MchPay {

    private static final Logger _log = LoggerFactory.getLogger(BaseNotify4MchPay.class);
    @Resource
    private MchNotifyMapper mchNotifyMapper;
    @Resource
    private MchInfoMapper mchInfoMapper;
    @Resource
    private PayOrderMapper payOrderMapper;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 创建响应URL
     *
     * @param payOrder
     * @param backType 1：前台页面；2：后台接口
     * @return
     */
    public String createNotifyUrl(PayOrder payOrder, String backType) {
        String mchId = payOrder.getMchId();
        MchInfo mchInfo = mchInfoMapper.selectByPrimaryKey(mchId);
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
            //todo 调用业务
//            receive(object.toJSONString());
//            BaldPayUtil.call4Post(object.getString("url"));
//            mq4MchPayNotify.send(object.toJSONString());
        } catch (Exception e) {
            _log.error("payOrderId={},sendMessage error.", ObjectUtils.defaultIfNull(payOrder.getPayOrderId(), ""));
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
        object.put("url", url);
        object.put("orderId", payOrder.getPayOrderId());
        object.put("count", count);
        object.put("createTime", System.currentTimeMillis());
        return object;
    }

    public void receive(String msg) {
        String logPrefix = "【商户支付通知】";
        _log.info("{}接收消息:msg={}", logPrefix, msg);
        JSONObject msgObj = JSON.parseObject(msg);
        String respUrl = msgObj.getString("url");
        String orderId = msgObj.getString("orderId");
        int count = msgObj.getInteger("count");
        if (StringUtils.isEmpty(respUrl)) {
            _log.warn("{}商户通知URL为空,respUrl={}", logPrefix, respUrl);
            return;
        }
        String httpResult = httpPost(respUrl);
        int cnt = count + 1;
        _log.info("{}notifyCount={}", logPrefix, cnt);
        if ("success".equalsIgnoreCase(httpResult)) {
            // 修改支付订单表
            try {
                PayOrder payOrder = new PayOrder();
                payOrder.setPayOrderId(orderId);
                payOrder.setStatus(PayConstant.PAY_STATUS_COMPLETE);
                PayOrderExample example = new PayOrderExample();
                PayOrderExample.Criteria criteria = example.createCriteria();
                criteria.andPayOrderIdEqualTo(orderId);
                criteria.andStatusEqualTo(PayConstant.PAY_STATUS_SUCCESS);
                int result = payOrderMapper.updateByExampleSelective(payOrder, example);
                _log.info("{}修改payOrderId={},订单状态为处理完成->{}", logPrefix, orderId, result == 1 ? "成功" : "失败");
            } catch (Exception e) {
                _log.error("修改订单状态为处理完成异常");
            }
            // 修改通知
            try {
                MchNotify mchNotify = new MchNotify();
                mchNotify.setStatus(PayConstant.MCH_NOTIFY_STATUS_SUCCESS);
                mchNotify.setResult(httpResult);
                mchNotify.setNotifyCount((byte) cnt);
                mchNotify.setLastNotifyTime(new Date());
                MchNotifyExample example = new MchNotifyExample();
                MchNotifyExample.Criteria criteria = example.createCriteria();
                criteria.andOrderIdEqualTo(orderId);
                List values = new LinkedList<>();
                values.add(PayConstant.MCH_NOTIFY_STATUS_NOTIFYING);
                values.add(PayConstant.MCH_NOTIFY_STATUS_FAIL);
                criteria.andStatusIn(values);
                int result = mchNotifyMapper.updateByExampleSelective(mchNotify, example);
                _log.info("{}修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, httpResult, cnt, result == 1 ? "成功" : "失败");
            } catch (Exception e) {
                _log.error("修改商户支付通知异常");
            }
            return; // 通知成功结束
        } else {
            // 修改通知次数
            try {
                MchNotify mchNotify = new MchNotify();
                mchNotify.setStatus(PayConstant.MCH_NOTIFY_STATUS_FAIL);
                mchNotify.setResult(httpResult);
                mchNotify.setNotifyCount((byte) cnt);
                mchNotify.setLastNotifyTime(new Date());
                MchNotifyExample example = new MchNotifyExample();
                MchNotifyExample.Criteria criteria = example.createCriteria();
                criteria.andOrderIdEqualTo(orderId);
                List values = new LinkedList<>();
                values.add(PayConstant.MCH_NOTIFY_STATUS_NOTIFYING);
                values.add(PayConstant.MCH_NOTIFY_STATUS_FAIL);
                int result = mchNotifyMapper.updateByExampleSelective(mchNotify, example);
                _log.info("{}修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, httpResult, cnt, result == 1 ? "成功" : "失败");
            } catch (Exception e) {
                _log.error("修改商户支付通知异常");
            }
            if (cnt > 5) {
                _log.info("{}通知次数notifyCount({})>5,停止通知", respUrl, cnt);
                return;
            }
            // 通知失败，延时再通知
            msgObj.put("count", cnt);
            //todo 再次通知
//            this.send(mchPayNotifyQueue, msgObj.toJSONString(), cnt * 60 * 1000);
            _log.info("{}发送延时通知完成,通知次数:{},{}秒后执行通知", respUrl, cnt, cnt * 60);
        }
    }

    public void receive1(String msg) {
        String logPrefix = "【商户支付通知】";
        _log.info("do notify task, msg={}", msg);
        JSONObject msgObj = JSON.parseObject(msg);
        String respUrl = msgObj.getString("url");
        String orderId = msgObj.getString("orderId");
        int count = msgObj.getInteger("count");
        if (StringUtils.isEmpty(respUrl)) {
            _log.warn("notify url is empty. respUrl={}", respUrl);
            return;
        }
        try {
            String notifyResult =null;
            _log.info("==>restTemplate通知业务系统开始[orderId：{}][count：{}][time：{}]", orderId, count, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            try {
                URI uri = new URI(respUrl);
                notifyResult = restTemplate.postForObject(uri, null, String.class);
            } catch (Exception e) {
                _log.error("通知商户系统异常");
            }
            _log.info("<==restTemplate通知业务系统结束[orderId：{}][count：{}][time：{}]", orderId, count, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            // 验证结果
            _log.info("notify response , OrderID={}", orderId);
            if (notifyResult != null && notifyResult.equalsIgnoreCase(PayConstant.RETURN_VALUE_SUCCESS)) {
                //_log.info("{} notify success, url:{}", _notifyInfo.getBusiId(), respUrl);
                //修改支付订单表
                try {
                    PayOrder payOrder = new PayOrder();
                    payOrder.setPayOrderId(orderId);
                    payOrder.setStatus(PayConstant.PAY_STATUS_COMPLETE);
                    PayOrderExample example = new PayOrderExample();
                    PayOrderExample.Criteria criteria = example.createCriteria();
                    criteria.andPayOrderIdEqualTo(orderId);
                    criteria.andStatusEqualTo(PayConstant.PAY_STATUS_SUCCESS);
                    int result = payOrderMapper.updateByExampleSelective(payOrder, example);
                    _log.info("修改payOrderId={},订单状态为处理完成->{}", orderId, result == 1 ? "成功" : "失败");
                } catch (Exception e) {
                    _log.error("修改订单状态为处理完成异常");
                }
                // 修改通知次数
                try {
                    MchNotify mchNotify = new MchNotify();
                    mchNotify.setStatus(PayConstant.MCH_NOTIFY_STATUS_SUCCESS);
                    mchNotify.setResult(notifyResult);
                    mchNotify.setNotifyCount((byte) 1);
                    mchNotify.setLastNotifyTime(new Date());
                    MchNotifyExample example = new MchNotifyExample();
                    MchNotifyExample.Criteria criteria = example.createCriteria();
                    criteria.andOrderIdEqualTo(orderId);
                    List values = new LinkedList<>();
                    values.add(PayConstant.MCH_NOTIFY_STATUS_NOTIFYING);
                    values.add(PayConstant.MCH_NOTIFY_STATUS_FAIL);
                    criteria.andStatusIn(values);
                    int result = mchNotifyMapper.updateByExampleSelective(mchNotify, example);
                    _log.info("{}修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, notifyResult, 1, result == 1 ? "成功" : "失败");
                } catch (Exception e) {
                    _log.error("修改商户支付通知异常");
                }
//                try {
//                    PayOrder newPayOrder = new PayOrder();
//                    newPayOrder.setNotifyCount((byte) 1);
//                    newPayOrder.setLastNotifyTime(System.currentTimeMillis());
//                    newPayOrder.setPayOrderId(orderId);
//                    int result = payOrderMapper.updateByPrimaryKeySelective(newPayOrder);
//                    _log.info("修改payOrderId={},通知业务系统次数->{}", orderId, result == 1 ? "成功" : "失败");
//                } catch (Exception e) {
//                    _log.error("修改通知次数异常");
//                }
                return; // 通知成功结束
            } else {
                // 通知失败，延时再通知
                int cnt = count + 1;
                _log.info("notify count={}", cnt);
                // 修改通知次数
                try {
                    MchNotify mchNotify = new MchNotify();
                    mchNotify.setStatus(PayConstant.MCH_NOTIFY_STATUS_FAIL);
                    mchNotify.setResult(notifyResult);
                    mchNotify.setNotifyCount((byte) cnt);
                    mchNotify.setLastNotifyTime(new Date());
                    MchNotifyExample example = new MchNotifyExample();
                    MchNotifyExample.Criteria criteria = example.createCriteria();
                    criteria.andOrderIdEqualTo(orderId);
//                    List values = new LinkedList<>();
//                    values.add(PayConstant.MCH_NOTIFY_STATUS_NOTIFYING);
//                    values.add(PayConstant.MCH_NOTIFY_STATUS_FAIL);
                    int result = mchNotifyMapper.updateByExampleSelective(mchNotify, example);
                    _log.info("{}修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, notifyResult, cnt, result == 1 ? "成功" : "失败");
                } catch (Exception e) {
                    _log.error("修改商户支付通知异常");
                }
//                try {
//                    PayOrder newPayOrder = new PayOrder();
//                    newPayOrder.setNotifyCount((byte) count);
//                    newPayOrder.setLastNotifyTime(System.currentTimeMillis());
//                    newPayOrder.setPayOrderId(orderId);
//                    int result = payOrderMapper.updateByPrimaryKeySelective(newPayOrder);
//                    _log.info("修改payOrderId={},通知业务系统次数->{}", orderId, result == 1 ? "成功" : "失败");
//                } catch (Exception e) {
//                    _log.error("修改通知次数异常");
//                }

                if (cnt > 5) {
                    _log.info("notify count>5 stop. url={}", respUrl);
                    return;
                }
                // 通知失败，延时再通知
                msgObj.put("count", cnt);
                //todo 再次通知
//                this.send(msgObj.toJSONString(), cnt * 60 * 1000);
                _log.info("{}发送延时通知完成,通知次数:{},{}秒后执行通知", respUrl, cnt, cnt * 60);
            }
            _log.warn("notify failed. url:{}, response body:{}", respUrl, notifyResult.toString());
        } catch (Exception e) {
            _log.info("<==MQ通知业务系统结束[orderId：{}][count：{}][time：{}]", orderId, count, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            _log.error("notify exception. url:{}", respUrl);
        }

    }

    public String httpPost(String url) {
        StringBuffer sb = new StringBuffer();
        try {
            URL console = new URL(url);
            if ("https".equals(console.getProtocol())) {
//                SSLContext sc = SSLContext.getInstance("SSL");
//                sc.init(null, new TrustManager[] { new TrustAnyTrustManager() },
//                        new java.security.SecureRandom());
//                HttpsURLConnection con = (HttpsURLConnection) console.openConnection();
//                con.setSSLSocketFactory(sc.getSocketFactory());
//                con.setRequestMethod("POST");
//                con.setDoInput(true);
//                con.setDoOutput(true);
//                con.setUseCaches(false);
//                con.setConnectTimeout(30 * 1000);
//                con.setReadTimeout(60 * 1000);
//                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()), 1024*1024);
//                while (true) {
//                    String line = in.readLine();
//                    if (line == null) {
//                        break;
//                    }
//                    sb.append(line);
//                }
//                in.close();
            } else if ("http".equals(console.getProtocol())) {
                HttpURLConnection con = (HttpURLConnection) console.openConnection();
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setConnectTimeout(30 * 1000);
                con.setReadTimeout(60 * 1000);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()), 1024 * 1024);
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                }
                in.close();
            } else {
                _log.error("not do protocol. protocol={}", console.getProtocol());
            }
        } catch (Exception e) {
            _log.error("httpPost exception. url:{}", url);
        }
        return sb.toString();
    }
}
