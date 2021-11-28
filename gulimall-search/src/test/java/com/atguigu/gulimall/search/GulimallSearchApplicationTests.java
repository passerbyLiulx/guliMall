package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.ElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

    /**
     * 存储数据到es
     */
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        User user = new User();
        user.setUsername("zhangsan");
        user.setAge(18);
        user.setGender("男");
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);
        // 执行操作
        IndexResponse index = client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);
        // 响应数据
        System.out.println(index);
    }

    /**
     * 查询es
     *
     * @throws IOException
     */
    @Test
    public void searchData() throws IOException {
        // 创建索引请求
        SearchRequest searchRequest = new SearchRequest();
        // 指定索引
        searchRequest.indices("bank");
        // 指定DSL检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构造索引条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mall"));
        // 按照年龄的值分布聚合
        TermsAggregationBuilder ageAggTermsAggregationBuilder = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAggTermsAggregationBuilder);
        // 按照平均薪资聚合
        AggregationBuilders.avg("balanceAvg").field("balance");

        searchSourceBuilder.from();
        searchSourceBuilder.size();
        searchSourceBuilder.aggregations();
        System.out.println(searchSourceBuilder.toString());
        searchRequest.source(searchSourceBuilder);
        // 执行检索
        SearchResponse searchResponse = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(searchResponse);
        Map map = JSON.parseObject(searchResponse.toString(), Map.class);
        // 分析结果
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitsArray = hits.getHits();
        for (SearchHit hit : hitsArray) {
            hit.getIndex();
            hit.getId();
            String sourceAsString = hit.getSourceAsString();
            Accout accout = JSON.parseObject(sourceAsString, Accout.class);
            System.out.println(accout);
        }
        Aggregations aggregations = searchResponse.getAggregations();
        for (Aggregation aggregation : aggregations.asList()) {

        }
        Terms ageAgg = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄：" + keyAsString + "==>" + bucket.getDocCount());
        }
        Avg balanceAvg = aggregations.get("balanceAvg");
        System.out.println("平均薪资：" + balanceAvg);

    }

    @Data
    class User {
        private String username;
        private String gender;
        private Integer age;
    }

    @ToString
    @Data
    static class Accout {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }
}
