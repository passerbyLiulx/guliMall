package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParamVo;
import com.atguigu.gulimall.search.vo.SearchResponseVo;

import javax.naming.directory.SearchResult;

public interface MallSearchService {

    SearchResponseVo search(SearchParamVo searchParamVo);
}
