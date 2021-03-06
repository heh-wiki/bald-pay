package wiki.heh.bald.pay.api.entity.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * @author heh
 * @date 2020/11/30
 */
@ApiModel("创建支付订单请求参数")
public class CreatePayOrderForm {

    @ApiModelProperty(value = "支付订单号", example = "")
    private String payOrderId;
    @ApiModelProperty(value = "商户ID", example = "")
    private String mchId;
    @ApiModelProperty(value = "商户订单号", example = "10000000")
    private String mchOrderNo;
    @ApiModelProperty(value = "渠道ID", example = "ALI_QR")
    private String channelId;
    @ApiModelProperty(value = "支付金额,单位分", example = "100")
    private Long amount;
    @ApiModelProperty(value = "三位货币代码,人民币:cny", example = "cny")
    private String currency;
    @ApiModelProperty(value = "客户端IP", example = "192.168.1.110")
    private String clientIp;
    @ApiModelProperty(value = "设备", example = "ios10.3.6")
    private String device;
    @ApiModelProperty(value = "商品标题", example = "伴置车测试商品")
    private String subject;
    @ApiModelProperty(value = "商品描述信息", example = "伴置车测试商品的描述信息")
    private String body;
    @ApiModelProperty(value = "特定渠道发起时额外参数", example = "{\"openId\":\"123\"}")
    private String extra;
//    @ApiModelProperty(value = "渠道商户ID", example = "")
//    private String channelMchId;
    @ApiModelProperty(value = "扩展参数1", example = "")
    private String param1;
    @ApiModelProperty(value = "扩展参数2", example = "")
    private String param2;
    @ApiModelProperty(value = "通知地址", example = "http://jiutongtang.cn:13020/goods/payNotify")
    private String notifyUrl;

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

    public String getMchOrderNo() {
        return mchOrderNo;
    }

    public void setMchOrderNo(String mchOrderNo) {
        this.mchOrderNo = mchOrderNo;
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
}
