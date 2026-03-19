package com.petcloud.map.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "map.amap")
public class AmapProperties {
    private String apiKey;
    private String baseUrl = "https://restapi.amap.com";
}
