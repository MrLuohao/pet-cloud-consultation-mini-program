package com.petcloud.user.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云百炼AI配置
 *
 * @author luohao
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyu.hundred-refinements")
public class AliYunAiConfig {
    /**
     * 百炼API Key
     */
    private String apiKey;
}
