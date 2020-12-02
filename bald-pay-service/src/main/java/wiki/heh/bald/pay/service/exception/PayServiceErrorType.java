package wiki.heh.bald.pay.service.exception;


/**
 * @author kugs
 */

public enum PayServiceErrorType {

    /**
     *
     */
    SYSTEM_ERROR                ("-1", "系统异常！"),
    MERCHANT_DOES_NOT_EXIST     ("900900", "商户不存在！"),
    PAY_ORDER_DOES_NOT_EXIST    ("900900", "支付订单不存在！"),
    PRIVATE_KEY_DOES_NOT_EXIST    ("900900", "商户私钥不存在！"),

    MISSING_REQUEST_PARAM_ERROR ("8811200", "参数缺失"),

    USER_NOT_FOUND              ("8811100", "用户未找到！"),
    ROLE_NOT_FOUND              ("8818101", "角色未找到！");

    /**
     * 错误类型码
     */
    private String code;
    /**
     * 错误类型描述信息
     */
    private String msg;

    PayServiceErrorType(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
