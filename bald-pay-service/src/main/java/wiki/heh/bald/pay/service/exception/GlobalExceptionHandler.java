package wiki.heh.bald.pay.service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ServerWebInputException;
import wiki.heh.bald.pay.service.model.vo.Result;


/**
 * 全局异常处理
 * @author hehua
 * @date 2020/11/4
 */
@ControllerAdvice(basePackages = "wiki.heh.bald.pay.service")
public class GlobalExceptionHandler {

    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 处理 ServiceException 异常
     */
    @ResponseBody
    @ExceptionHandler(value = ServiceException.class)
    public Result serviceExceptionHandler(ServiceException ex) {
        log.debug("[serviceExceptionHandler]", ex);
        // 包装 CommonResult 结果
        return Result.fail(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理 ServerWebInputException 异常
     *
     * WebFlux 参数不正确
     */
    @ResponseBody
    @ExceptionHandler(value = ServerWebInputException.class)
    public Result serverWebInputExceptionHandler(ServerWebInputException ex) {
        log.debug("[ServerWebInputExceptionHandler]", ex);
        // 包装 CommonResult 结果
        return Result.fail(PayServiceErrorType.MISSING_REQUEST_PARAM_ERROR);
    }

    /**
     * 处理其它 Exception 异常
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Result exceptionHandler(Exception e) {
        // 记录异常日志
        log.error("[exceptionHandler]", e);
        // 返回 ERROR CommonResult
        return Result.fail(PayServiceErrorType.SYSTEM_ERROR);
    }

}
