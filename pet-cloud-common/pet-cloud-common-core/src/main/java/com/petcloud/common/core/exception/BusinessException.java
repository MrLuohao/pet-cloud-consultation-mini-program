package com.petcloud.common.core.exception;

import com.petcloud.common.core.response.IRespType;
import com.petcloud.common.core.utils.NullSafeUtil;
import lombok.Getter;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.Serial;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 业务异常类
 *
 * @author luohao
 */
@Getter
public class BusinessException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8734819164020549416L;

    private final IRespType respType;
    private FormattingTuple formattingTuple;

    public BusinessException(IRespType respType) {
        super(resolveRespTypeOrDefault(respType).getMessage());
        this.respType = resolveRespTypeOrDefault(respType);
    }

    public BusinessException(Throwable throwable) {
        super(throwable);
        this.respType = RespType.BUSINESS_ERROR;
    }

    public BusinessException(IRespType respType, Object... args) {
        this.respType = resolveRespType(respType);
        this.formattingTuple = createFormattingTuple(respType, args);
        initCause(this.formattingTuple.getThrowable());
    }

    @Deprecated
    public BusinessException(String msgPattern, Object... args) {
        this.respType = RespType.BUSINESS_ERROR;
        this.formattingTuple = MessageFormatter.arrayFormat(msgPattern, args);
        initCause(this.formattingTuple.getThrowable());
    }

    private IRespType resolveRespType(IRespType respType) {
        return resolveRespTypeOrDefault(respType);
    }

    private static IRespType resolveRespTypeOrDefault(IRespType respType) {
        return respType == null ? RespType.BUSINESS_ERROR : respType;
    }

    private FormattingTuple createFormattingTuple(IRespType respType, Object... args) {
        AtomicReference<Object[]> finalArgsRef = new AtomicReference<>(args);

        // 判断是否使用自定义消息模式
        String messagePattern = Optional.ofNullable(args)
                .filter(arr -> arr.length > 0 && arr[0] instanceof String)
                .map(arr -> (String) arr[0])
                .filter(pattern -> {
                    // 判断是否应该使用自定义模式
                    // 条件：自定义模式有占位符 或者 默认消息没有占位符
                    boolean shouldUseCustom = pattern.contains("{}") || !respType.getMessage().contains("{}");

                    if (shouldUseCustom) {
                        // 如果使用自定义模式，则从第二个元素开始作为参数
                        finalArgsRef.set(trimFirstElement(args));
                    }
                    return shouldUseCustom;
                })
                .orElseGet(respType::getMessage);

        // 获取最终参数
        Object[] finalArgs = finalArgsRef.get();

        return MessageFormatter.arrayFormat(messagePattern, finalArgs);
    }

    private Object[] trimFirstElement(Object[] args) {
        if (args == null || args.length <= 1) {
            return new Object[0];
        }
        return Arrays.copyOfRange(args, 1, args.length);
    }

    @Override
    public String getMessage() {
        if (super.getMessage() != null) {
            return super.getMessage();
        }
        return formattingTuple != null ?
                formattingTuple.getMessage() :
                respType.getMessage();
    }
}
