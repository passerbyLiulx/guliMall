package com.atguigu.gulimall.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SeckillSkuScheduled {

    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Days() {
         
    }
}
