package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.OrderStatusEnum;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.seata.spring.annotation.GlobalTransactional;
import jdk.nashorn.internal.ir.CallNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUserThreadLocal.get();
        // 获取之前的请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 远程查询所有的收货地址列表
            List<MemberAddressVo> addressList = memberFeignService.getAddressList(memberRespVo.getId());
            orderConfirmVo.setAddressVoList(addressList);
        }, threadPoolExecutor);
        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 远程查询购物车所有选中的购物项
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setItemVoList(currentUserCartItems);
        }, threadPoolExecutor).thenRunAsync(() -> {
            List<OrderItemVo> itemVoList = orderConfirmVo.getItemVoList();
            List<Long> skuIdList = itemVoList.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R result = wmsFeignService.getSkusHasStock(skuIdList);
            List<SkuHasStockVo> skuHasStockVoList = result.getData(new TypeReference<List<SkuHasStockVo>>() {
            });
            if (skuHasStockVoList != null && skuHasStockVoList.size() > 0) {
                Map<Long, Boolean> skuHasStockMap = skuHasStockVoList.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::isHasStock));
                orderConfirmVo.setStockMap(skuHasStockMap);
            }
        }, threadPoolExecutor);

        // 查询用户积分
        Integer integration = memberRespVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token);
        orderConfirmVo.setOrderToken(token);

        CompletableFuture.allOf(addressFuture, cartFuture).get();

        return orderConfirmVo;
    }

    @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUserThreadLocal.get();
        // 验证令牌
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = orderSubmitVo.getOrderToken();
        // 原子验证令牌和删除令牌
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        if (result == 0L) {
            // 令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            // 创建订单
            OrderCreateVo orderCreateVo = createOrder();
            BigDecimal payAmount = orderCreateVo.getOrderEntity().getPayAmount();
            BigDecimal payPrice = orderSubmitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 保存订单
                saveOrder(orderCreateVo);
                // 库存锁定
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(orderCreateVo.getOrderEntity().getOrderSn());
                List<OrderItemVo> locks = orderCreateVo.getOrderItemEntityList().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                // 为了保证高并发，库存服务自己回滚，可以发消息给库存服务
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    responseVo.setOrderEntity(orderCreateVo.getOrderEntity());
                    return responseVo;
                } else {
                    // 锁失败
                    throw new NoStockException();
                    //responseVo.setCode(3);
                    //return responseVo;
                }
            } else {
                // 金额对比失败
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    /**
     * 保存订单
     * @param orderCreateVo
     */
    private void saveOrder(OrderCreateVo orderCreateVo) {
        OrderEntity orderEntity = orderCreateVo.getOrderEntity();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> orderItemEntityList = orderCreateVo.getOrderItemEntityList();
        orderItemService.saveBatch(orderItemEntityList);
    }

    /**
     * 创建订单
     * @return
     */
    private OrderCreateVo createOrder() {
        OrderCreateVo orderCreateVo = new OrderCreateVo();
        String orderSn = IdWorker.getTimeId();
        // 订单信息
        OrderEntity orderEntity = buildOrder(orderSn);
        // 订单项
        List<OrderItemEntity> orderItemEntityList = buildOrderItemList(orderSn);
        // 验价
        computePrice(orderEntity, orderItemEntityList);

        orderCreateVo.setOrderEntity(orderEntity);
        orderCreateVo.setOrderItemEntityList(orderItemEntityList);
        return orderCreateVo;
    }

    /**
     * 构建订单
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUserThreadLocal.get();
        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(memberRespVo.getId());
        orderEntity.setOrderSn(orderSn);
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareData = fare.getData(new TypeReference<FareVo>(){});
        orderEntity.setFreightAmount(fareData.getFare());
        orderEntity.setReceiverCity(fareData.getAddressVo().getCity());
        orderEntity.setReceiverDetailAddress(fareData.getAddressVo().getDetailAddress());
        orderEntity.setReceiverName(fareData.getAddressVo().getNickname());
        orderEntity.setReceiverPhone(fareData.getAddressVo().getMobile());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);

        return orderEntity;
    }

    /**
     * 构建订单项列表
     */
    private List<OrderItemEntity> buildOrderItemList(String orderSn) {
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = buildOrderItem(cartItem);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());

            return itemEntities;
        }
        return null;
    }

    /**
     * 构建一个订单项
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        // 订单信息
        //  商品的spu信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setCategoryId(data.getCatalogId());
        // 商品的sku信息
        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImages());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttrsVals = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrsVals);
        orderItemEntity.setSkuQuantity(cartItem.getCount());
        // 优惠信息
        // 积分信息
        orderItemEntity.setGiftGrowth(cartItem.getPrice().intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().intValue());
        // 价格
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        BigDecimal realAmount = new BigDecimal(orderItemEntity.getSkuQuantity().toString())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount())
                .subtract(orderItemEntity.getPromotionAmount());
        orderItemEntity.setRealAmount(realAmount);
        return orderItemEntity;
    }

    /**
     * 验价
     * @param orderEntity
     * @param orderItemEntityList
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntityList) {
        // 订单价格相关的
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");

        for (OrderItemEntity orderItemEntity : orderItemEntityList) {
            coupon = coupon.add(orderItemEntity.getCouponAmount());
            integration = integration.add(orderItemEntity.getIntegrationAmount());
            promotion = promotion.add(orderItemEntity.getPromotionAmount());
            total = total.add(orderItemEntity.getRealAmount());
            gift = gift.add(new BigDecimal(orderItemEntity.getGiftIntegration().toString()));
            growth = growth.add(new BigDecimal(orderItemEntity.getGiftGrowth().toString()));
        }
        orderEntity.setTotalAmount(total);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setDeleteStatus(0);

    }


}