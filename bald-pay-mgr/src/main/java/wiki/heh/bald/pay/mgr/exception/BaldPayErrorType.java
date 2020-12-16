package wiki.heh.bald.pay.mgr.exception;


/**
 * @author kugs
 */

public enum BaldPayErrorType {

    /**
     *
     */
    SYSTEM_ERROR                ("-1", "系统异常！"),

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

    BaldPayErrorType(String code, String msg) {
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
