package com.petcloud.common.web.handler;

import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.response.Response;
import org.apache.catalina.connector.ClientAbortException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author luohao
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 客户端主动断开（常见于视频分片下载中止、切换页面）
     * 这类异常不应再包装成业务响应，避免触发二次写回错误。
     */
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException ex) {
        log.warn("客户端中断连接: {}", ex.getMessage());
    }

    /**
     * 处理 @RequestBody 参数验证异常
     * 对应 @Validated 或 @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<List<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();

        List<String> errors = bindingResult.getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        log.error("参数验证失败: {}", errors);

        return Response.of(RespType.PARAMETER_ERROR, errors, "参数验证失败");
    }

    /**
     * 处理 @ModelAttribute 参数验证异常
     * 对应 @Validated 在方法参数上
     */
    @ExceptionHandler(BindException.class)
    public Response<List<String>> handleBindException(BindException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        log.error("绑定参数验证失败: {}", errors);

        return Response.of(RespType.PARAMETER_ERROR, errors, "参数绑定验证失败");
    }

    /**
     * 处理 @RequestParam/@PathVariable 参数验证异常
     * 对应 @NotBlank, @NotNull, @Min, @Max 等注解
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Response<List<String>> handleConstraintViolationException(ConstraintViolationException ex) {

        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String path = violation.getPropertyPath().toString();
                    // 提取简单字段名
                    String field = path.contains(".")
                            ? path.substring(path.lastIndexOf('.') + 1)
                            : path;
                    return String.format("%s: %s", field, violation.getMessage());
                })
                .collect(Collectors.toList());

        log.error("参数约束验证失败: {}", errors);

        return Response.of(RespType.PARAMETER_ERROR, errors, "参数约束验证失败");
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Response<?> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return Response.of(ex.getRespType(), ex.getMessage());
    }

    /**
     * 处理参数类型不匹配（如 limit=undefined）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Response<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("参数类型不匹配: {}", ex.getMessage());
        return Response.of(RespType.PARAMETER_ERROR, "参数类型错误");
    }

    /**
     * 处理未认证异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public Response<?> handleIllegalStateException(IllegalStateException ex) {
        if ("用户未认证".equals(ex.getMessage())) {
            return Response.of(RespType.TOKEN_INVALID);
        }
        log.warn("状态异常: {}", ex.getMessage());
        return Response.of(RespType.BUSINESS_ERROR, ex.getMessage());
    }

    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Response<?> handleGenericException(Exception ex) {
        log.error("系统异常: ", ex);
        String msg = ex.getClass().getSimpleName();
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            msg = msg + ": " + ex.getMessage();
        }
        return Response.of(RespType.FAILURE, msg);
    }
}
