package wiki.heh.bald.pay.mgr.exception;


/**
 * 服务异常
 * @author heh
 * @date 2020/11/4
 */
public final class MgrException extends RuntimeException {

    /**
     * 错误码
     */
    private final String code;

    public MgrException(MgrErrorType serviceExceptionEnum) {
        // 使用父类的 message 字段
        super(serviceExceptionEnum.getMsg());
        // 设置错误码
        this.code = serviceExceptionEnum.getCode();
    }

    public String getCode() {
        return code;
    }

}
