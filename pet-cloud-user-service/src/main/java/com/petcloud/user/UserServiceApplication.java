package com.petcloud.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.util.StopWatch;

import java.text.DecimalFormat;

/**
 * 用户服务启动类
 *
 * @author luohao
 */
@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = {"com.petcloud.common.web", "com.petcloud.user"})
@ComponentScan(basePackages = {"com.petcloud.common.web", "com.petcloud.user"})
public class UserServiceApplication {

    public static void main(String[] args) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ApplicationContext ctx = SpringApplication.run(UserServiceApplication.class, args);
        Environment env = ctx.getEnvironment();

        stopWatch.stop();

        String appName = env.getProperty("spring.application.name", "PetCloudUserService");
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
