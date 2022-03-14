package com.atguigu.gulimall.feign.fallback;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.feign.CouponFeignService;
import org.springframework.stereotype.Component;

@Component
public class CouponFeignServiceFallBack implements CouponFeignService {

    @Override
    public R getLates3DaySession() {
        return R.error(BizCodeEnume.TO_MANY_REQUEST.getCode(), BizCodeEnume.TO_MANY_REQUEST.getMsg());
    }
}
