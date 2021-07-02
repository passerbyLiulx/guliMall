package com.atguigu.gulimall.search.service.impl;

import com.atguigu.gulimall.search.config.ElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParamVo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchResult;
import java.io.IOException;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParamVo searchParamVo) {
        SearchResult result = null;
        // 准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParamVo);
        try {
            // 执行检索请求
            SearchResponse response = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            // 分析响应数据
            result = buildSearchResult(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private SearchResult buildSearchResult(SearchResponse response) {
        return null;
    }

    /**
     * 检索请求
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
        // 聚合

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }
}
