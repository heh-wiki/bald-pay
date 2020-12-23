package wiki.heh.bald.pay.api.entity.form;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author heh
 * @date 2020/11/30
 */
@ApiModel("统一下单请求参数")
public class AliQrForm {

    @ApiModelProperty(value = "商户订单号(商户生成的订单号)",required = true, example = "20160427210604000490")
    private String payOrderId;
    @ApiModelProperty(value = "商户ID(支付中心分配的商户号)",required = true, example = "10000000")
    private String mchId;
    @ApiModelProperty(value = "渠道ID(见支付渠道参数)",required = true, example = "WX_JSAPI")
    private String channelId;
    @ApiModelProperty(value = "币种(三位货币代码,人民币:cny)",required = true, example = "cny")
    private String currency;
    @ApiModelProperty(value = "客户端IP(客户端IP地址)", example = "10.73.10.148")
    private String clientIp;
    @ApiModelProperty(value = "设备", example = "ios10.3.1")
    private String device;
    @ApiModelProperty(value = "支付结果回调URL",required = true, example = "http://jiutongtang.cn/notify.htm")
    private String notifyUrl;
    @ApiModelProperty(value = "商品主题",required = true, example = "伴置车测试商品1")
    private String subject;
    @ApiModelProperty(value = "商品描述信息",required = true, example = "伴置车项目测试商品描述")
    private String body;
    @ApiModelProperty(value = "支付金额,单位分",required = true, example = "1000")
    private Long amount;
    @ApiModelProperty(value = "扩展参数1", example = "")
    private String param1;
    @ApiModelProperty(value = "扩展参数2", example = "")
    private String param2;
    @ApiModelProperty(value = "特定渠道发起时额外参数",required = true, example = "{“openId”:”o2RvowBf7sOVJf8kJksUEMceaDqo”}")
    private String extra;
    @ApiModelProperty(value = "签名",required = true, example = "C380BEC2BFD727A4B6845133519F3AD6")
    private String sign;

    public String getPayOrderId() {
        return payOrderId;
    }

    public void setPayOrderId(String payOrderId) {
        this.payOrderId = payOrderId;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
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

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
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

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
