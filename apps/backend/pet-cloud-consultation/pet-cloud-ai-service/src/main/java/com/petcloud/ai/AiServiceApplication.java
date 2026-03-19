package com.petcloud.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.util.StopWatch;

import java.text.DecimalFormat;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = {"com.petcloud.ai", "com.petcloud.common.web"})
@ComponentScan(basePackages = {"com.petcloud.ai", "com.petcloud.common.web"})
public class AiServiceApplication {

    public static void main(String[] args) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ApplicationContext ctx = SpringApplication.run(AiServiceApplication.class, args);
        Environment env = ctx.getEnvironment();

        stopWatch.stop();

        log.info("""
                         ==================== 启动成功 ====================
                         应用: \t{}
                         环境: \t{}
                         端口: \t{}
                         耗时: \t{} 秒
                         =======================================================""",
                env.getProperty("spring.application.name", "PetCloudAiService"),
                String.join(",", env.getActiveProfiles()),
                env.getProperty("server.port", "8080"),
                new DecimalFormat("#.##").format(stopWatch.getTotalTimeSeconds()));
    }
}
