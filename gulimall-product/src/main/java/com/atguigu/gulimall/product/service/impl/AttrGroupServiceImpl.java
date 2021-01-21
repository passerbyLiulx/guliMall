package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        IPage<AttrGroupEntity> page = null;
        if (catelogId == 0) {
            page  = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    new QueryWrapper<AttrGroupEntity>());
        } else {
            String key = (String) params.get("key");
            QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId);
            if (!StringUtils.isEmpty(key)) {
                queryWrapper.and((obj) -> {
                    obj.eq("attr_group_id", key).or().like("attr_group_name", key);
                });
            }
            page  = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    queryWrapper);
        }
        return new PageUtils(page);
    }

}