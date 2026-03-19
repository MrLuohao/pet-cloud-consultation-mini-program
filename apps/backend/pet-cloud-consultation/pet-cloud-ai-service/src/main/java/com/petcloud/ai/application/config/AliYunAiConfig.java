package com.petcloud.ai.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyu.hundred-refinements")
public class AliYunAiConfig {

    private String apiKey;
}
