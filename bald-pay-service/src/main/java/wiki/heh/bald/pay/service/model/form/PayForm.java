package wiki.heh.bald.pay.service.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author heh
 * @date 2020/11/30
 */
@ApiModel("统一下单请求参数")
public class PayForm {

    @ApiModelProperty(value = "商户订单号(商户生成的订单号)", required = true, example = "20160427210604000490")
    private String payOrderId;
    @ApiModelProperty(value = "签名", required = true, example = "C380BEC2BFD727A4B6845133519F3AD6")
    private String sign;

    public String getPayOrderId() {
        return payOrderId;
    }

    public void setPayOrderId(String payOrderId) {
        this.payOrderId = payOrderId;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
