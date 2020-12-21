package wiki.heh.bald.pay.service.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author heh
 * @date 2020/12/4
 */
@ApiModel("统一支付请求参数")
public class TestUnifiedPayForm {

    @ApiModelProperty(value = "支付类型【1 = 支付宝】【2 = 微信】【3 = 钱包】", example = "1")
    int type;
    @ApiModelProperty(value = "支付金额（分）", example = "999")
    String fee;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }
}
