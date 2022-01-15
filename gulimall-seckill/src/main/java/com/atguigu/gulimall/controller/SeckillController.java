package com.atguigu.gulimall.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.Service.SeckillService;
import com.atguigu.gulimall.vo.SeckillSkuVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {

        List<SeckillSkuVo> seckillSkuVoList =  seckillService.getCurrentSeckillSkus();
        return R.ok().setData(seckillSkuVoList);
    }

    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {

        SeckillSkuVo seckillSkuVo = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(seckillSkuVo);
    }

    public R seckill(@RequestParam("killId") String killId,
                     @RequestParam("key") String key,
                     @RequestParam("num") Integer num) {
        String orderSn = seckillService.kill(killId, key, num);

        return R.ok().setData(orderSn);
    }

}
