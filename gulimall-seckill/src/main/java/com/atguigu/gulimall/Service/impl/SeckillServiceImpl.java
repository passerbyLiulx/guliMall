package com.atguigu.gulimall.Service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.Service.SeckillService;
import com.atguigu.gulimall.feign.CouponFeignService;
import com.atguigu.gulimall.vo.SeckillSessionWithSkus;
import com.atguigu.gulimall.vo.SeckillSkuVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";

    @Override
    public void uploadseckillSkuLatest3Days() {
        R lates3DaySession = couponFeignService.getLates3DaySession();
        if (lates3DaySession.getCode() == 0) {
            // 上架商品
            List<SeckillSessionWithSkus> sessionDataList = lates3DaySession.getData(new TypeReference<List<SeckillSessionWithSkus>>() {
            });
            // 缓存活动信息
            saveSessionInfos(sessionDataList);
            // 缓存活动的关联商品信息
            saveSessionSkuInfos(sessionDataList);


        }
    }

    @Override
    public List<SeckillSkuVo> getCurrentSeckillSkus() {
        // 1.确定当前时间属于哪个秒杀场次
        long time = new Date().getTime();
        Set<String> keys = stringRedisTemplate.keys(SESSIONS_CACHE_PREFIX);
        for (String key : keys) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);
            if (time >= start && time <= end) {
                // 2.获取这个秒杀场次需要的所有商品信息
                List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps("");
                List<String> objects = hashOps.multiGet(range);
                if (objects != null && objects.size() > 0) {
                    List<SeckillSkuVo> collect = objects.stream().map(item -> {
                        SeckillSkuVo seckillSkuVo = JSON.parseObject(item.toString(), SeckillSkuVo.class);
                        return seckillSkuVo;
                    }).collect(Collectors.toList());
                    return collect;
                }

                break;
            }
        }


        return null;
    }

    @Override
    public SeckillSkuVo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SESSIONS_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" +skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SeckillSkuVo seckillSkuVo = JSON.parseObject(json, SeckillSkuVo.class);
                    // 随机码匹配对返回
                    return seckillSkuVo;
                }
            }

        }



        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SESSIONS_CACHE_PREFIX);
        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        } else {
            JSON.parseObject(json, SeckillSkuVo.class);
            // 校验时间合法性
            // 校验随机码和商品id，前端传的和库中是否一致
            // 验证购物数量
            // 是否购买过
            rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", "");

        }

        return null;

    }

    private void saveSessionInfos(List<SeckillSessionWithSkus> sessionDataList) {
        sessionDataList.stream().forEach(session -> {
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            Boolean aBoolean = stringRedisTemplate.hasKey(key);
            if (aBoolean) {
                List<String> collect = session.getSeckillSkuVos().stream().map(item -> item.getId().toString()).collect(Collectors.toList());
                stringRedisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionWithSkus> sessionDataList) {

    }

}
