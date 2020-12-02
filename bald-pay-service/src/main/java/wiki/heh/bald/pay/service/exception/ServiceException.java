package wiki.heh.bald.pay.service.exception;


/**
 * 服务异常
 * @author hehua
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

    public String getCode() {
        return code;
    }

}
