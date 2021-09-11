package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面传递过来的查询条件
 * catalog3Id=225&keyword=小米&sort=saleCount_asc&hasStock=0/1
 */
@Data
public class SearchParamVo {

    private String keyword;  // 全文匹配关键字

    private Long catalog3Id; // 三级分类Id

    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_acs/desc
     */
    private String sort;  // 排序

    private Integer hasStock = 1;  // 是否有货  0:无库存 1:有库存

    private String skuPrice;  // 价格区间  格式 1_500/_500/500_

    private List<Long> brandId;  // 品牌Id

    private List<String> attrs;  // 属性筛选

    private Integer pageNum = 1; // 页码

    private String queryString;  // 原生的所有查询条件


}
