package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateVo {

    private OrderEntity orderEntity;

    private List<OrderItemEntity> orderItemEntityList;

    private BigDecimal payPrice;

    private BigDecimal fare;
}
