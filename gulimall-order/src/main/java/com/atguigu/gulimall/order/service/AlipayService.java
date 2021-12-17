package com.atguigu.gulimall.order.service;

import com.alipay.api.AlipayApiException;

public interface AlipayService {

    /**
     * 统一收单交易支付接口
     * @param outTradeNo 订单编号（唯一）
     *      * @param totalAmount 订单价格
     *      * @param subject 商品名称
     * @return
     * @throws Exception
     */
    String pay(String outTradeNo, String totalAmount, String subject) throws Exception;

    /**
     * 手机网站支付接口2.0
     * @param outTradeNo 订单编号（唯一）
     * @param totalAmount 订单价格
     * @param subject 商品名称
     * @return
     * @throws Exception
     */
    String wapPay(String outTradeNo, Integer totalAmount, String subject) throws Exception;

    /**
     * app支付接口2.0
     * @param outTradeNo 订单编号（唯一）
     * @param totalAmount 订单价格
     * @param subject 商品名称
     * @return
     * @throws Exception
     */
    String appPay(String outTradeNo, Integer totalAmount, String subject) throws Exception;

    /**
     * 统一收单线下交易查询
     * @param outTradeNo 订单编号（唯一）
     * @return
     * @throws AlipayApiException
     */
    String query(String outTradeNo) throws AlipayApiException;

    /**
     * 统一收单交易退款接口
     * @param outTradeNo 订单编号
     * @param refundReason 退款原因
     * @param refundAmount 退款金额
     * @param outRequestNo 标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传
     * @return
     * @throws AlipayApiException
     */
    String refund(String outTradeNo, String refundReason, Integer refundAmount, String outRequestNo) throws AlipayApiException;

    /**
     * 统一收单交易退款查询
     * @param outTradeNo 订单编号（唯一）
     * @param outRequestNo 标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传
     * @return
     * @throws AlipayApiException
     */
    String refundQuery(String outTradeNo,String outRequestNo) throws AlipayApiException;

    /**
     * 统一收单交易关闭接口
     * @param outTradeNo 订单编号（唯一）
     * @return
     * @throws AlipayApiException
     */
    String close(String outTradeNo) throws AlipayApiException;


}