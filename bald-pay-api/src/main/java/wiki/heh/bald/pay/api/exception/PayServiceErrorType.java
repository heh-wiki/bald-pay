package wiki.heh.bald.pay.api.exception;


/**
 * @author kugs
 */

public enum PayServiceErrorType {

    /**
     *
     */
    SYSTEM_ERROR                    ("-1", "系统异常！"),
    MERCHANT_DOES_NOT_EXIST         ("900900", "商户不存在！"),
    PAY_ORDER_DOES_NOT_EXIST        ("900900", "支付订单不存在！"),
    THIS_ORDER_IS_PAYING            ("900900", "订单正在支付或已支付！"),
    PRIVATE_KEY_DOES_NOT_EXIST      ("900900", "商户私钥不存在！"),

    MISSING_REQUEST_PARAM_ERROR     ("8811200", "参数缺失"),

    USER_NOT_FOUND                  ("8811100", "用户未找到！"),
    ROLE_NOT_FOUND                  ("8818101", "角色未找到！"),

    // 0000: 成功
    RET_SUCCESS                     ("000000", ""),

    // 失败(00开始标示通讯层相关错误码)
    RET_REMOTE_UNUSABLE             ("880001", "远程服务不可用"),
    RET_REMOTE_INVALID              ("880002", "客户端非法调用"),
    RET_NO_BIZ_SEQUENCE_NO          ("880003", "远程服务调用业务流水号不存在"),
    RET_REMOTE_CHECK_SIGN_FAIL      ("880004", "远程服务调用签名验证失败"),
    RET_REMOTE_RPC_SEQ_NO_REPEATED  ("880005", "随机通讯码在指定时间内重复"),
    RET_REMOTE_SIGN_INVALID         ("880006", "远程服务调用签名计算方式错误"),
    RET_REMOTE_DEAL_EXCEPTION       ("880007", "远程服务调用处理异常"),
    RET_REMOTE_PROTOCOL_INVALID     ("880008", "客户端调用协议非法"),
    RET_REMOTE_HTTP_METHOD_INVALID  ("880009", "客户端请求方式非法"),

    // 失败(01开始标示参数校验相关错误码)
    RET_PARAM_NOT_FOUND             ("880101", "参数不存在"),
    RET_PARAM_INVALID               ("880102", "无效的参数"),
    RET_PARAM_TOO_LARGE_LIST        ("880103", "列表超长"),
    RET_PARAM_TYPE_INVALID          ("880104", "参数类型错误"),
    RET_CURRENT_PAGE_INVALID        ("880105", "当前页码非法"),
    RET_VIEW_NUMBER_INVALID         ("880106", "分页显示数目非法"),
    RET_VIEW_LIMIT_INVALID          ("880107", "数据排列显示数目非法"),

    //  失败(02开始标示DB操作相关错误码)
    RET_DB_FAIL                     ("880201", "数据库操作失败"),

    // 业务相关
    RET_BIZ_DATA_NOT_EXISTS         ("881001", "数据不存在"),
    RET_BIZ_SING_DATA_FAIL          ("881002", "商户签名数据不正确"),
    RET_BIZ_PAY_CREATE_FAIL         ("881003", "支付下单失败"),
    RET_BIZ_WX_PAY_CREATE_FAIL      ("881003", "微信支付下单失败"),
    RET_BIZ_ALI_PAY_CREATE_FAIL     ("881004", "支付宝支付下单失败"),
    RET_BIZ_PAY_NOTIFY_VERIFY_FAIL  ("881005", "支付通知数据验证不正确"),
    FAILED_TO_CREATE_PAYMENT_ORDER  ("881006", "创建支付订单失败"),

    THIRD_PARTY_EXCEPTION           ("881400", "第三方异常"),


    // 未知错误
    RET_UNKNOWN_ERROR               ("9999", "未知错误");
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
