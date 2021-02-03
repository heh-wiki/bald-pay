package wiki.heh.bald.pay.api.exception;

import com.thoughtworks.xstream.core.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ServerWebInputException;
import wiki.heh.bald.pay.api.entity.vo.Result;

/**
 * 全局异常处理
 * @author heh
 * @date 2020/11/4
 */
//@ControllerAdvice(basePackages = "wiki.heh.bald.pay.api")
@RestControllerAdvice
public class GlobalExceptionHandlerAdvice{

    private Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result exception() {
        return Result.fail();
    }

    @ExceptionHandler(value = {Throwable.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result throwable() {
        return Result.fail();
    }
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

}
