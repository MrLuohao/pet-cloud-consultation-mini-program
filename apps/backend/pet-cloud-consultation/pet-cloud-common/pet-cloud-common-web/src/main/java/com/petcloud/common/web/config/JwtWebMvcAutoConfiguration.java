package com.petcloud.common.web.config;

import com.petcloud.common.web.interceptor.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 自动配置类
 * 自动配置JWT认证拦截器
 *
 * @author luohao
 */
@Configuration
@RequiredArgsConstructor
public class JwtWebMvcAutoConfiguration implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    /**
     * 默认排除的路径（不需要认证）
     */
    private static final String[] DEFAULT_EXCLUDE_PATHS = {
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/internal/**",
            "/error",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(getExcludePaths());
    }

    /**
     * 获取排除路径，子类可以覆盖此方法添加更多排除路径
     *
     * @return 排除路径数组
     */
    protected String[] getExcludePaths() {
        return DEFAULT_EXCLUDE_PATHS;
    }
}
