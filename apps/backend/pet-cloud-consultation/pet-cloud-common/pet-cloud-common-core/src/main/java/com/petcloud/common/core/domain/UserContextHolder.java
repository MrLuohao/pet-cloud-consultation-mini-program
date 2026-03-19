package com.petcloud.common.core.domain;

/**
 * 用户上下文持有者 - 基于 ThreadLocal 存储当前线程的用户信息
 * 用于在同一请求线程中共享用户信息，支持 MyBatis-Plus 自动填充等场景
 *
 * @author luohao
 */
public class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户上下文
     *
     * @param userContext 用户上下文
     */
    public static void setContext(UserContext userContext) {
        CONTEXT_HOLDER.set(userContext);
    }

    /**
     * 获取当前用户上下文
     *
     * @return 用户上下文，如果未设置则返回 null
     */
    public static UserContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，如果未设置则返回 null
     */
    public static Long getUserId() {
        UserContext context = getContext();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 获取当前用户昵称
     *
     * @return 用户昵称，如果未设置则返回 null
     */
    public static String getNickname() {
        UserContext context = getContext();
        return context != null ? context.getNickname() : null;
    }

    /**
     * 清除当前用户上下文
     * 必须在请求结束时调用，防止内存泄漏
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
