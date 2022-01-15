package com.atguigu.gulimall.Service;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.vo.SeckillSkuVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


public interface SeckillService {

    public void uploadseckillSkuLatest3Days();

    List<SeckillSkuVo> getCurrentSeckillSkus();

    SeckillSkuVo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
