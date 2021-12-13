package com.atguigu.gulimall.order.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * 提交订单
 * 购买的商品去购物车再获取一次
 * 用户相关信息直接去session取
 */
@Data
public class OrderSubmitVo {

    private Long addrId;  //收货地址的id

    private Integer payType;  //支付方式

    private String orderToken;  //防重令牌

    private BigDecimal payPrice;  //应付价格  验价

    //

}
