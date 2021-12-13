package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {

    private OrderEntity orderEntity;

    private Integer code;
}
