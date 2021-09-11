package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {

    /**
     * 基本信息
     */
    private SkuInfoEntity info;

    /**
     * 图片信息
     */
    private List<SkuImagesEntity> images;

    /**
     * 介绍
     */
    private SpuInfoDescEntity desc;

    /**
     * 销售属性组合
     */
    private List<SkuItemSaleAttrVo> saleAttr;

    /**
     * 规格参数信息
     */
    List<SpuItemAttrGroupVo> groupAttrs;

    public static class SpuBaseAttrVo {
        private String attrName;
        private String attrValue;
    }

}
