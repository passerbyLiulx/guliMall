package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 *
 * @author liulx
 * @email 1191026928@qq.com
 * @date 2020-12-22 19:24:55
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {

}
