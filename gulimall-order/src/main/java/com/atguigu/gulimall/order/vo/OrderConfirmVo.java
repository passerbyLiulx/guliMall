package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {

    /**
     * 收货地址列表
     */
    @Setter @Getter
    List<MemberAddressVo> addressVoList;

    /**
     * 购物项列表
     */
    @Setter @Getter
    List<OrderItemVo> itemVoList;

    /**
     * 优惠券
     */
    @Setter @Getter
    Integer integration;

    /**
     * 库存
     */
    @Setter @Getter
    Map<Long, Boolean> stockMap;

    /**
     * 防重令牌
     */
    @Setter @Getter
    String orderToken;

    /**
     * 总额
     */
    BigDecimal total;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (itemVoList != null && itemVoList.size() > 0) {
            for (OrderItemVo item : itemVoList) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum.add(multiply);
            }
        }
        return sum;
    }

    /**
     * 应付价格
     */
    BigDecimal payPrice;

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
