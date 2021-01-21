package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 菜单树
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        // 查询所有分类
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);

        // 组装树形结构
        List<CategoryEntity> categoryEntityListWithTree = categoryEntityList.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map(menu -> {
            menu.setChildren(getChildren(menu, categoryEntityList));
            return menu;
        }).sorted((menu1, menu2) ->
            (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())
        ).collect(Collectors.toList());

        return categoryEntityListWithTree;
    }

    /**
     * 子菜单
     * @param root
     * @param all
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == root.getCatId()
        ).map(categoryEntity -> {
            // 递归
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) ->
            (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())
        ).collect(Collectors.toList());

        return children;
    }

    @Override
    public void removeMenuByIds(List<Long> catIdList) {

        // TODO 检查当前菜单是否被其他地方引用

        // 逻辑删除
        baseMapper.deleteBatchIds(catIdList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        paths = findParentPath(catelogId, paths);
        Collections.reverse(paths);
        return paths.toArray(new Long[(paths.size())]);
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            findParentPath(categoryEntity.getParentCid(), paths);
        }
        return paths;
    }
}