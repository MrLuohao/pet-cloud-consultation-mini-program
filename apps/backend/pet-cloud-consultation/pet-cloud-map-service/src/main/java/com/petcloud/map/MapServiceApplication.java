package com.petcloud.map;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        MybatisPlusAutoConfiguration.class
})
@ConfigurationPropertiesScan(basePackages = {"com.petcloud.common.web", "com.petcloud.map"})
@ComponentScan(basePackages = {"com.petcloud.common.web", "com.petcloud.map"})
public class MapServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MapServiceApplication.class, args);
    }
}
