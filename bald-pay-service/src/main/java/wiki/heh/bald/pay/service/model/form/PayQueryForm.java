package wiki.heh.bald.pay.service.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author hehua
 * @date 2020/12/1
 */
@ApiModel("支付查询请求接口")
public class PayQueryForm {

    @ApiModelProperty(value = "商户id",example = "10000000")
    private String mchId;
    @ApiModelProperty(value = "支付订单id",example = "1234567891234564")
    private String payOrderId;
    @ApiModelProperty(value = "商户订单号",example = "321321321321")
    private String mchOrderNo;
    @ApiModelProperty(value = "是否回调",example = "false")
    private Boolean executeNotify;

    public Boolean getExecuteNotify() {
        return executeNotify;
    }

    public void setExecuteNotify(Boolean executeNotify) {
        this.executeNotify = executeNotify;
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
}
