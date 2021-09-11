package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResponseVo {

    private List<SkuEsModel> products;  // 所有商品

    private Integer pageNum;  // 当前页码
    private Integer total;  // 总记录数
    private Integer totalPages;  // 总页数
    private List<Integer> pageNavs;  // 导航页码
    private List<BrandVo> brands;  // 所有品牌
    private List<CatalogVo> catalogs;  // 所有分类
    private List<AttrVo> attrs;  // 所有属性
    private List<NavVo> navs = new ArrayList<>();  // 面包屑导航数据

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

}
