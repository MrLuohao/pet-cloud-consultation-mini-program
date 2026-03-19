package com.petcloud.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 聊天异步线程池配置
 */
@Configuration
public class ChatAsyncConfig {

    @Bean("chatSseExecutor")
    public Executor chatSseExecutor() {
        return new ThreadPoolExecutor(
                4,
                16,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("chat-sse-" + thread.getId());
                    thread.setDaemon(false);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
