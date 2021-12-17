package com.atguigu.common.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockedVo {

    /**
     * 库存工作单id
     */
    private Long id;

    /**
     * 工作单详情
     */
    private StockLockedDetailVo detail;
}
