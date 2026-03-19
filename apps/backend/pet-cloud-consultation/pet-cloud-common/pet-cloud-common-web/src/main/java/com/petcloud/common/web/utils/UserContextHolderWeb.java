package com.petcloud.common.web.utils;

import com.petcloud.common.web.config.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 用户上下文工具类（Web层）
 * 用于从请求中获取当前登录用户的信息
 *
 * @author luohao
 */
@Component
public class UserContextHolderWeb {

    private final JwtProperties jwtProperties;

    public UserContextHolderWeb(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * 从请求中获取当前用户ID
     *
     * @param request HttpServletRequest
     * @return 用户ID，如果未认证则返回null
     */
    public Long getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(jwtProperties.getUserIdAttribute());
        if (userId == null) {
            // 尝试从请求头获取（用于服务间调用）
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader != null) {
                try {
                    return Long.parseLong(userIdHeader);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
        if (userId instanceof Long) {
            return (Long) userId;
        }
        if (userId instanceof String) {
            try {
                return Long.parseLong((String) userId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取当前用户ID，如果未认证则抛出异常
     *
     * @param request HttpServletRequest
     * @return 用户ID
     * @throws IllegalStateException 如果用户未认证
     */
    public Long getRequiredUserId(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            throw new IllegalStateException("用户未认证");
        }
        return userId;
    }

    /**
     * 获取当前用户ID，如果未认证则抛出异常
     * （getStrictUserId的别名，保持兼容性）
     *
     * @param request HttpServletRequest
     * @return 用户ID
     * @throws IllegalStateException 如果用户未认证
     */
    public Long getStrictUserId(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            throw new IllegalStateException("用户未认证");
        }
        return userId;
    }

    /**
     * 设置当前用户ID到请求属性中
     *
     * @param request HttpServletRequest
     * @param userId  用户ID
     */
    public void setCurrentUserId(HttpServletRequest request, Long userId) {
        request.setAttribute(jwtProperties.getUserIdAttribute(), userId);
    }
}
