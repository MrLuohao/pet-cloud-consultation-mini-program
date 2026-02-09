package com.petcloud.common.web.handler;

import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * MyBatis-Plus 异常处理器
 *
 * @author luohao
 */
@Slf4j
@RestControllerAdvice
public class MybatisPlusExceptionHandler {

    /**
     * 处理 MyBatis-Plus 相关异常
     */
    @ExceptionHandler({
            com.baomidou.mybatisplus.core.exceptions.MybatisPlusException.class,
            org.apache.ibatis.exceptions.PersistenceException.class
    })
    public Response<?> handleMybatisPlusException(Exception ex) {
        log.error("数据库操作异常: ", ex);
        return Response.of(RespType.DATABASE_ERROR, ex.getMessage());
    }
}
