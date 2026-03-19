package com.petcloud.map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = {"com.petcloud.common.web", "com.petcloud.map"})
@ComponentScan(basePackages = {"com.petcloud.common.web", "com.petcloud.map"})
public class MapServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MapServiceApplication.class, args);
    }
}
