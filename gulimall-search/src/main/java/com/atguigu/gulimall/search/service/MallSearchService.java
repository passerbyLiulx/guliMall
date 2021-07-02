package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParamVo;

import javax.naming.directory.SearchResult;

public interface MallSearchService {

    SearchResult search(SearchParamVo searchParamVo);
}
