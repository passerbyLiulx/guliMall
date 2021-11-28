package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 *
 * @author liulx
 * @email 1191026928@qq.com
 * @date 2020-12-23 10:32:10
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

}
