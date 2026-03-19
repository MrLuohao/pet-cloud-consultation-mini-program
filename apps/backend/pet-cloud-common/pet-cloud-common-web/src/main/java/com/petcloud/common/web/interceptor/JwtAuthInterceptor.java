package com.petcloud.common.web.interceptor;

import com.petcloud.common.core.domain.UserContext;
import com.petcloud.common.core.domain.UserContextHolder;
import com.petcloud.common.web.config.JwtProperties;
import com.petcloud.common.web.constant.CacheConstants;
import com.petcloud.common.web.utils.JwtUtils;
import com.petcloud.common.web.utils.RedisUtil;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 * 统一处理JWT Token验证和用户身份识别
 *
 * @author luohao
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final RedisUtil redisUtil;
    private final JwtProperties jwtProperties;
    private final UserContextHolderWeb userContextHolderWeb;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader(jwtProperties.getHeader());

        // 放行OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 如果没有Authorization头，直接放行（有些接口可能不需要认证）
        if (authHeader == null || authHeader.isEmpty()) {
            log.debug("没有Authorization头，放行请求");
            return true;
        }

        try {
            String token = authHeader;
            String prefix = jwtProperties.getTokenPrefix();
            if (prefix != null && authHeader.startsWith(prefix)) {
                token = authHeader.substring(prefix.length());
            }

            // 去除Token两端的空白字符
            token = token != null ? token.trim() : "";

            // 检查token是否为空
            if (token.isEmpty()) {
                log.warn("Token为空，无法认证");
                return true;
            }

            // 检查Token是否在黑名单中（已登出）
            String blacklistKey = CacheConstants.TOKEN_BLACKLIST_PREFIX + token;
            if (redisUtil.hasKey(blacklistKey)) {
                log.warn("Token已在黑名单中，拒绝访问");
                sendUnauthorizedResponse(response, "登录已过期，请重新登录");
                return false;
            }

            // 验证token
            if (jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserIdFromToken(token);
                String nickname = jwtUtils.getNicknameFromToken(token);

                // 将userId设置到请求属性中，供Controller使用
                userContextHolderWeb.setCurrentUserId(request, userId);

                // 同时设置到 ThreadLocal 中，供 MyBatis-Plus 自动填充使用
                UserContextHolder.setContext(UserContext.builder()
                        .userId(userId)
                        .nickname(nickname)
                        .build());

                log.info("JWT认证成功，userId: {}", userId);
            } else {
                log.warn("Token验证失败，返回401");
                sendUnauthorizedResponse(response, "Token无效或已过期，请重新登录");
                return false;
            }
        } catch (Exception e) {
            log.warn("JWT认证失败", e);
            sendUnauthorizedResponse(response, "认证失败，请重新登录");
            return false;
        }

        return true;
    }

    /**
     * 发送401未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"status\":false,\"code\":\"401\",\"msg\":\"%s\"}",
                message
        ));
    }

    /**
     * 请求完成后清理 ThreadLocal，防止内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        UserContextHolder.clear();
    }
}
