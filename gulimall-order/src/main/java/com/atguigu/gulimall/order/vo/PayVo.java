package com.atguigu.gulimall.order.vo;

import lombok.Data;

@Data
public class PayVo {

    private String out_trade_no;  // 商品订单号
    private String subject;  // 订单名称
    private String total_amount;  // 付款金额
    private String body;  // 商品描述
}
