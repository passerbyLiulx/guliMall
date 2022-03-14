package com.atguigu.gulimall.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.feign.fallback.CouponFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "gulimall-coupon", fallback = CouponFeignServiceFallBack.class)
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/lates3DaySession")
    public R getLates3DaySession();

}
