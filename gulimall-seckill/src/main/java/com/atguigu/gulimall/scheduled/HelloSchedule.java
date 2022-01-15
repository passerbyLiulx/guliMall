package com.atguigu.gulimall.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class HelloSchedule {

    /**
     * 1.spring中6位组成，不允许第7位的年
     * 2.在周的位置，1-7代表周一到周日，
     * 3.定时任务
     * 异步+定时任务实现不阻塞功能
     */
    @Async
    @Scheduled(cron = "* 1 * * * *")
    public void hello() throws InterruptedException {
        CompletableFuture.runAsync(() -> {

        });
        log.info("hello...");
        Thread.sleep(3000);
    }
}
