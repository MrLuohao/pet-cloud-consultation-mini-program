package com.petcloud.common.web.handler;

import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public Response<List<String>> handleConstraintViolationException(
            jakarta.validation.ConstraintViolationException ex) {

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
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Response<?> handleGenericException(Exception ex) {
        log.error("系统异常: ", ex);
        return Response.of(RespType.FAILURE, ex.getMessage());
    }
}
