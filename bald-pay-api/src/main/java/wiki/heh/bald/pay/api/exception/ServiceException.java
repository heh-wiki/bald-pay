package wiki.heh.bald.pay.api.exception;


/**
 * 服务异常
 *
 * @author heh
 * @date 2020/11/4
 */
public final class ServiceException extends RuntimeException {

    /**
     * 错误码
     */
    private final String code;

    public ServiceException(PayServiceErrorType serviceExceptionEnum) {
        // 使用父类的 message 字段
        super(serviceExceptionEnum.getMsg());
        // 设置错误码
        this.code = serviceExceptionEnum.getCode();
    }

    /**
     * 第三方异常
     */
    public ServiceException( String msg) {
        // 使用父类的 message 字段
        super(msg);
        // 设置错误码
        this.code = "400400";
    }

    public String getCode() {
        return code;
    }

}
