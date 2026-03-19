package com.petcloud.shop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.util.StopWatch;

import java.text.DecimalFormat;

/**
 * 商城服务启动类
 *
 * @author luohao
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = {"com.petcloud.common.web", "com.petcloud.shop"})
@ComponentScan(basePackages = {"com.petcloud.common.web", "com.petcloud.shop"})
@EnableFeignClients(basePackages = "com.petcloud.shop.infrastructure.feign")
public class ShopServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(ShopServiceApplication.class);

    public static void main(String[] args) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ApplicationContext ctx = SpringApplication.run(ShopServiceApplication.class, args);
        Environment env = ctx.getEnvironment();

        stopWatch.stop();

        String appName = env.getProperty("spring.application.name", "PetCloudShopService");
        String activeProfile = String.join(",", env.getActiveProfiles());
        if (activeProfile.isEmpty()) {
            activeProfile = "default";
        }

        log.info("""
                        \s
                         ==================== 启动成功 ====================
                         应用: \t{}
                         环境: \t{}
                         端口: \t{}
                         耗时: \t{} 秒
                         =======================================================""",
                appName,
                activeProfile,
                env.getProperty("server.port", "8080"),
                new DecimalFormat("#.##").format(stopWatch.getTotalTimeSeconds()));
    }
}
