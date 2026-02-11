package com.petcloud.common.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置属性类
 * 从配置文件读取JWT相关配置
 *
 * @author luohao
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT签名密钥
     * 建议在生产环境中使用环境变量或配置中心管理
     */
    private String secretKey = "pet-cloud-consultation-secret-key-for-jwt-token-generation-2024";

    /**
     * Token过期时间（毫秒）
     * 默认7天
     */
    private long expirationTime = 7 * 24 * 60 * 60 * 1000;

    /**
     * Token请求头名称
     */
    private String header = "Authorization";

    /**
     * Token前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Token请求属性名（用于存储userId）
     */
    private String userIdAttribute = "currentUserId";
}
