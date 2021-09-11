package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.ElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.AttrResponseVo;
import com.atguigu.gulimall.search.vo.SearchParamVo;
import com.atguigu.gulimall.search.vo.SearchResponseVo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResponseVo search(SearchParamVo searchParamVo) {
        SearchResponseVo searchResponseVo = null;
        // 准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParamVo);
        try {
            // 执行检索请求
            SearchResponse response = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            // 分析响应数据
            searchResponseVo = buildSearchResult(response, searchParamVo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResponseVo;
    }

    /**
     * 准备检索请求
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParamVo paramVo) {
        // 构建DSL语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(paramVo.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", paramVo.getKeyword()));
        }
        if (paramVo.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", paramVo.getCatalog3Id()));
        }
        if (paramVo.getBrandId() != null && paramVo.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", paramVo.getBrandId()));
        }
        if (paramVo.getAttrs() != null && paramVo.getAttrs().size() > 0) {
            // attrs=1_5寸:8寸&attrs=2_16G:8G
            for (String attrStr : paramVo.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }
        }
        boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", paramVo.getHasStock()));
        if (StringUtils.isNotBlank(paramVo.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = paramVo.getSkuPrice().split("_");
            if (s.length == 2) {
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (paramVo.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
                if (paramVo.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQueryBuilder.filter(rangeQuery);
        }
        // 排序
        if (StringUtils.isNotBlank(paramVo.getSort())) {
            // sort=hotScore_asc/desc
            String[] s = paramVo.getSort().split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(s[0], order);
        }
        // 分页
        searchSourceBuilder.from((paramVo.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        // 高亮
        if (StringUtils.isNotBlank(paramVo.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            searchSourceBuilder.highlighter(builder);
        }

        // 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brand_agg);
        // 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalog_agg);
        // 属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        searchSourceBuilder.aggregation(attr_agg);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }

    /**
     * 构建结果数据
     *
     * @param response
     * @return
     */
    private SearchResponseVo buildSearchResult(SearchResponse response, SearchParamVo searchParamVo) {
        SearchHits hits = response.getHits();
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        // 商品
        List<SkuEsModel> esModelList = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (StringUtils.isNotBlank(searchParamVo.getKeyword())) {
                    String HighlightSkuTitle = hit.getHighlightFields().get("skuTitle").getFragments()[0].string();
                    skuEsModel.setSkuTitle(HighlightSkuTitle);
                }
                esModelList.add(skuEsModel);
            }
        }
        searchResponseVo.setProducts(esModelList);
        // 属性信息
        List<SearchResponseVo.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResponseVo.AttrVo attrVo = new SearchResponseVo.AttrVo();
            // 属性Id
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);
            // 属性名称
            String attr_name_agg = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attr_name_agg);
            // 属性的所有值
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> ((Terms.Bucket) item).getKeyAsString()).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        searchResponseVo.setAttrs(attrVos);
        // 品牌信息
        List<SearchResponseVo.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResponseVo.BrandVo brandVo = new SearchResponseVo.BrandVo();
            // 品牌Id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            // 品牌名称
            String brand_name_agg = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brand_name_agg);
            // 品牌图片
            String brand_img_agg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brand_img_agg);
            brandVos.add(brandVo);
        }
        searchResponseVo.setBrands(brandVos);
        // 分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResponseVo.CatalogVo> calalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResponseVo.CatalogVo catalogVo = new SearchResponseVo.CatalogVo();
            // 分类Id
            catalogVo.setCatalogId(Long.parseLong(bucket.getKeyAsString()));
            // 分类名
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            catalogVo.setCatalogName(catalog_name_agg.getBuckets().get(0).getKeyAsString());
            calalogVos.add(catalogVo);
        }
        searchResponseVo.setCatalogs(calalogVos);
        // 页码
        searchResponseVo.setPageNum(searchParamVo.getPageNum());
        // 总记录数
        int total = (int) hits.getTotalHits().value;
        searchResponseVo.setTotal(total);
        // 总页码
        int totalPages = total % EsConstant.PRODUCT_PAGESIZE == 0 ? total % EsConstant.PRODUCT_PAGESIZE : total % EsConstant.PRODUCT_PAGESIZE + 1;
        searchResponseVo.setTotalPages(totalPages);
        // 导航页码
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i < totalPages; i++) {
            pageNavs.add(i);
        }
        searchResponseVo.setPageNavs(pageNavs);
        // 面包屑导航
        if (searchParamVo.getAttrs() != null && searchParamVo.getAttrs().size() > 0) {
            List<SearchResponseVo.NavVo> navVoList = searchParamVo.getAttrs().stream().map(attr -> {
                SearchResponseVo.NavVo navVo = new SearchResponseVo.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.getAttrsInfo(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }
                String attrEncode = null;
                try {
                    attrEncode = URLEncoder.encode(attr, "UTF-8");
                    attrEncode = attrEncode.replace("+", "%20");  // 浏览器对空格编码和java不一样
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String replace = searchParamVo.getQueryString().replace("&attrs=" + attrEncode, "");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());

            searchResponseVo.setNavs(navVoList);
        }
        return searchResponseVo;
    }

}










