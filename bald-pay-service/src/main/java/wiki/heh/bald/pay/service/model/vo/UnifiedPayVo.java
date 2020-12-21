package wiki.heh.bald.pay.service.model.vo;

import io.swagger.annotations.ApiModel;

/**
 * @author heh
 * @date 2020/12/4
 */
@ApiModel("统一支付返回参数")
public class UnifiedPayVo {
    private String sign;

    /**
     * timeStamp : 1564995114
     * packageValue : Sign=WXPay
     * appId : wx1e7c7e74f64ea9e0
     * prepayId : wx051651540388770ad7feadae1839865000
     * partnerId : 1520947621
     * nonceStr : mip3n1bvvjrqwjgrvs7hgggy4
     */
    private String timeStamp;
    private String packageValue;
    private String appId;
    private String prepayId;
    private String partnerId;
    private String nonceStr;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPackageValue() {
        return packageValue;
    }

    public void setPackageValue(String packageValue) {
        this.packageValue = packageValue;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPrepayId() {
        return prepayId;
    }

    public void setPrepayId(String prepayId) {
        this.prepayId = prepayId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }
}
