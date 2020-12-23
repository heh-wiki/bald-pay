package wiki.heh.bald.pay.api.entity.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author heh
 * @date 2020/12/21
 */
@ApiModel("统一退款请求参数")
public class UnifiedRefundForm {
    @ApiModelProperty(value = "商户ID", example = "10000000")
    private String mchId;
    @ApiModelProperty(value = "支付订单号", example = "P2020120221060400046")
    private String payOrderId;
    @ApiModelProperty(value = "商户订单号", example = "2020120221060400046")
    private String mchOrderNo;
    @ApiModelProperty(value = "商户退款单号", required = true, example = "2020120221060400046")
    private String mchRefundNo;
    @ApiModelProperty(value = "渠道ID", required = true, example = "ALIPAY_QR")
    private String channelId;
    @ApiModelProperty(value = "退款金额（单位分）", required = true, example = "99999")
    private Long amount;
    @ApiModelProperty(value = "三位货币代码,人民币:cny", example = "cny")
    private String currency;
    @ApiModelProperty(value = "客户端IP", example = "192.168.1.110")
    private String clientIp;
    @ApiModelProperty(value = "设备", example = "ios10.3.6")
    private String device;
    @ApiModelProperty(value = "特定渠道发起时额外参数", example = "{\"openId\":\"123\"}")
    private String extra;
    @ApiModelProperty(value = "扩展参数1", example = "")
    private String param1;
    @ApiModelProperty(value = "扩展参数2", example = "")
    private String param2;
    @ApiModelProperty(value = "通知地址", required = true, example = "http://jiutongtang.cn:19060/test")
    private String notifyUrl;
    @ApiModelProperty(value = "签名", required = true, example = "ADKJHFKJAV<MNVA")
    private String sign;
    @ApiModelProperty(value = "渠道用户标识,如微信openId,支付宝账号", required = true, example = "ADKJHFKJAV<MNVA")
    private String channelUser;
    @ApiModelProperty(value = "用户姓名", required = true, example = "张三")
    private String userName;
    @ApiModelProperty(value = "备注", required = true, example = "取消订单退款")
    private String remarkInfo;

    @Override
    public String toString() {
        return "UnifiedRefundForm{" +
                "mchId='" + mchId + '\'' +
                ", payOrderId='" + payOrderId + '\'' +
                ", mchOrderNo='" + mchOrderNo + '\'' +
                ", mchRefundNo='" + mchRefundNo + '\'' +
                ", channelId='" + channelId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", device='" + device + '\'' +
                ", extra='" + extra + '\'' +
                ", param1='" + param1 + '\'' +
                ", param2='" + param2 + '\'' +
                ", notifyUrl='" + notifyUrl + '\'' +
                ", sign='" + sign + '\'' +
                ", channelUser='" + channelUser + '\'' +
                ", userName='" + userName + '\'' +
                ", remarkInfo='" + remarkInfo + '\'' +
                '}';
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getPayOrderId() {
        return payOrderId;
    }

    public void setPayOrderId(String payOrderId) {
        this.payOrderId = payOrderId;
    }

    public String getMchOrderNo() {
        return mchOrderNo;
    }

    public void setMchOrderNo(String mchOrderNo) {
        this.mchOrderNo = mchOrderNo;
    }

    public String getMchRefundNo() {
        return mchRefundNo;
    }

    public void setMchRefundNo(String mchRefundNo) {
        this.mchRefundNo = mchRefundNo;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getChannelUser() {
        return channelUser;
    }

    public void setChannelUser(String channelUser) {
        this.channelUser = channelUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRemarkInfo() {
        return remarkInfo;
    }

    public void setRemarkInfo(String remarkInfo) {
        this.remarkInfo = remarkInfo;
    }
}
