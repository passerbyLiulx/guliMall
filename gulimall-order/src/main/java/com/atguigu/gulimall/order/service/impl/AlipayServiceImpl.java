package com.atguigu.gulimall.order.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.*;
import com.alipay.api.response.AlipayTradePayResponse;
import com.atguigu.gulimall.order.config.AlipayConfig;
import com.atguigu.gulimall.order.service.AlipayService;
import org.springframework.stereotype.Service;

@Service
public class AlipayServiceImpl implements AlipayService {

    /**
     * 调取支付宝接口
     */
    AlipayClient alipayClient = new DefaultAlipayClient(
            AlipayConfig.GATEWAY_URL, AlipayConfig.APP_ID, AlipayConfig.MERCHANT_PRIVATE_KEY, "json", AlipayConfig.CHARSET, AlipayConfig.ALIPAY_PUBLIC_KEY, AlipayConfig.SIGN_TYPE);

    /**
     * 统一收单交易支付接口
     * @param outTradeNo 订单编号（唯一）
     *      * @param totalAmount 订单价格
     *      * @param subject 商品名称
     * @return
     * @throws Exception
     */
    @Override
    public String pay(String outTradeNo, String totalAmount, String subject) throws Exception {
        AlipayTradePayRequest alipayTradePayRequest = new AlipayTradePayRequest();
        // 同步通知，支付完成后，支付成功页面
        alipayTradePayRequest.setReturnUrl(AlipayConfig.RETURN_URL);
        // 异步通知，支付完成后，需要进行的异步处理
        alipayTradePayRequest.setNotifyUrl(AlipayConfig.NOTIFY_URL);
        // 除公共参数外所有请求参数
        alipayTradePayRequest.setBizContent("{" +
                "\"out_trade_no\":\"20150320010101011\"," +
                "\"scene\":\"bar_code\"," +
                "\"auth_code\":\"28763443825664394\"," +
                "\"product_code\":\"FACE_TO_FACE_PAYMENT\"," +
                "\"subject\":\"Iphone6 16G\"," +
                "\"buyer_id\":\"2088202954065786\"," +
                "\"seller_id\":\"2088102146225135\"," +
                "\"total_amount\":88.88," +
                "\"trans_currency\":\"USD\"," +
                "\"settle_currency\":\"USD\"," +
                "\"discountable_amount\":8.88," +
                "\"undiscountable_amount\":80.00," +
                "\"body\":\"Iphone6 16G\"," +
                "      \"goods_detail\":[{" +
                "        \"goods_id\":\"apple-01\"," +
                "\"alipay_goods_id\":\"20010001\"," +
                "\"goods_name\":\"ipad\"," +
                "\"quantity\":1," +
                "\"price\":2000," +
                "\"goods_category\":\"34543238\"," +
                "\"categories_tree\":\"124868003|126232002|126252004\"," +
                "\"body\":\"特价手机\"," +
                "\"show_url\":\"http://www.alipay.com/xxx.jpg\"" +
                "        }]," +
                "\"operator_id\":\"yx_001\"," +
                "\"store_id\":\"NJ_001\"," +
                "\"terminal_id\":\"NJ_T_001\"," +
                "\"alipay_store_id\":\"2016041400077000000003314986\"," +
                "\"extend_params\":{" +
                "\"sys_service_provider_id\":\"2088511833207846\"," +
                "\"hb_fq_num\":\"3\"," +
                "\"hb_fq_seller_percent\":\"100\"," +
                "\"industry_reflux_info\":\"{\\\\\\\"scene_code\\\\\\\":\\\\\\\"metro_tradeorder\\\\\\\",\\\\\\\"channel\\\\\\\":\\\\\\\"xxxx\\\\\\\",\\\\\\\"scene_data\\\\\\\":{\\\\\\\"asset_name\\\\\\\":\\\\\\\"ALIPAY\\\\\\\"}}\"," +
                "\"card_type\":\"S0JP0000\"" +
                "    }," +
                "\"timeout_express\":\"90m\"," +
                "\"agreement_params\":{" +
                "\"agreement_no\":\"20170322450983769228\"," +
                "\"auth_confirm_no\":\"423979\"," +
                "\"apply_token\":\"MDEDUCT0068292ca377d1d44b65fa24ec9cd89132f\"" +
                "    }," +
                "\"royalty_info\":{" +
                "\"royalty_type\":\"ROYALTY\"," +
                "        \"royalty_detail_infos\":[{" +
                "          \"serial_no\":1," +
                "\"trans_in_type\":\"userId\"," +
                "\"batch_no\":\"123\"," +
                "\"out_relation_id\":\"20131124001\"," +
                "\"trans_out_type\":\"userId\"," +
                "\"trans_out\":\"2088101126765726\"," +
                "\"trans_in\":\"2088101126708402\"," +
                "\"amount\":0.1," +
                "\"desc\":\"分账测试1\"," +
                "\"amount_percentage\":\"100\"" +
                "          }]" +
                "    }," +
                "\"settle_info\":{" +
                "        \"settle_detail_infos\":[{" +
                "          \"trans_in_type\":\"cardAliasNo\"," +
                "\"trans_in\":\"A0001\"," +
                "\"summary_dimension\":\"A0001\"," +
                "\"settle_entity_id\":\"2088xxxxx;ST_0001\"," +
                "\"settle_entity_type\":\"SecondMerchant、Store\"," +
                "\"amount\":0.1" +
                "          }]," +
                "\"settle_period_time\":\"7d\"" +
                "    }," +
                "\"sub_merchant\":{" +
                "\"merchant_id\":\"2088000603999128\"," +
                "\"merchant_type\":\"alipay: 支付宝分配的间连商户编号, merchant: 商户端的间连商户编号\"" +
                "    }," +
                "\"disable_pay_channels\":\"credit_group\"," +
                "\"merchant_order_no\":\"201008123456789\"," +
                "\"auth_no\":\"2016110310002001760201905725\"," +
                "\"ext_user_info\":{" +
                "\"name\":\"李明\"," +
                "\"mobile\":\"16587658765\"," +
                "\"cert_type\":\"IDENTITY_CARD\"," +
                "\"cert_no\":\"362334768769238881\"," +
                "\"min_age\":\"18\"," +
                "\"fix_buyer\":\"F\"," +
                "\"need_check_info\":\"F\"" +
                "    }," +
                "\"auth_confirm_mode\":\"COMPLETE：转交易支付完成结束预授权;NOT_COMPLETE：转交易支付完成不结束预授权\"," +
                "\"terminal_params\":\"{\\\"key\\\":\\\"value\\\"}\"," +
                "\"promo_params\":{" +
                "\"actual_order_time\":\"2018-09-25 22:47:33\"" +
                "    }," +
                "\"advance_payment_type\":\"ENJOY_PAY_V2\"," +
                "      \"query_options\":[" +
                "        \"voucher_detail_list\"" +
                "      ]," +
                "\"business_params\":{" +
                "\"campus_card\":\"0000306634\"," +
                "\"card_type\":\"T0HK0000\"," +
                "\"actual_order_time\":\"2019-05-14 09:18:55\"" +
                "    }," +
                "\"request_org_pid\":\"2088201916734621\"," +
                "\"is_async_pay\":false" +
                "  }");
        /*alipayTradePayRequest.setBizContent("{\"out_trade_no\":\""+ outTradeNo +"\","
                + "\"total_amount\":\""+ totalAmount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\"商品信息\","
                + "\"timeout_express\":\"90m\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");*/
        AlipayTradePayResponse response = alipayClient.execute(alipayTradePayRequest);
        if(response.isSuccess()){
            return "调用成功";
        } else {
            return "调用失败";
        }
    }


    /**
     * 手机网站支付接口2.0
     * @param outTradeNo 订单编号（唯一）
     * @param totalAmount 订单价格
     * @param subject 商品名称
     * @return
     * @throws Exception
     */
    @Override
    public String wapPay(String outTradeNo, Integer totalAmount, String subject) throws Exception {
        AlipayTradeWapPayRequest alipayTradeWapPayRequest = new AlipayTradeWapPayRequest();
        // 同步通知，支付完成后，支付成功页面
        alipayTradeWapPayRequest.setReturnUrl(AlipayConfig.RETURN_URL);
        // 异步通知，支付完成后，需要进行的异步处理
        alipayTradeWapPayRequest.setNotifyUrl(AlipayConfig.NOTIFY_URL);
        // 除公共参数外所有请求参数
        alipayTradeWapPayRequest.setBizContent("{\"out_trade_no\":\""+ outTradeNo +"\","
                + "\"total_amount\":\""+ totalAmount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\"商品信息\","
                + "\"timeout_express\":\"90m\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String submitFormData = alipayClient.pageExecute(alipayTradeWapPayRequest).getBody();
        return submitFormData;
    }

    /**
     * app支付接口2.0
     * @param outTradeNo 订单编号（唯一）
     * @param totalAmount 订单价格
     * @param subject 商品名称
     * @return
     * @throws Exception
     */
    @Override
    public String appPay(String outTradeNo, Integer totalAmount, String subject) throws Exception {
        AlipayTradeAppPayRequest alipayTradeAppPayRequest = new AlipayTradeAppPayRequest();
        // 同步通知，支付完成后，支付成功页面
        alipayTradeAppPayRequest.setReturnUrl(AlipayConfig.RETURN_URL);
        // 异步通知，支付完成后，需要进行的异步处理
        alipayTradeAppPayRequest.setNotifyUrl(AlipayConfig.NOTIFY_URL);
        // 除公共参数外所有请求参数
        alipayTradeAppPayRequest.setBizContent("{\"out_trade_no\":\""+ outTradeNo +"\","
                + "\"total_amount\":\""+ totalAmount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\"商品信息\","
                + "\"timeout_express\":\"90m\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String submitFormData = alipayClient.pageExecute(alipayTradeAppPayRequest).getBody();
        return submitFormData;
    }

    /**
     * 统一收单线下交易查询
     * @param outTradeNo 订单编号（唯一）
     * @return
     * @throws AlipayApiException
     */
    @Override
    public String query(String outTradeNo) throws AlipayApiException {
        AlipayTradeQueryRequest alipayTradeQueryRequest = new AlipayTradeQueryRequest();
        alipayTradeQueryRequest.setBizContent("{\"out_trade_no\":\""+ outTradeNo +"\","+"\"trade_no\":\""+ "" +"\"}");
        String result = alipayClient.execute(alipayTradeQueryRequest).getBody();
        return result;
    }

    /**
     * 统一收单交易退款接口
     * @param outTradeNo 订单编号
     * @param refundReason 退款原因
     * @param refundAmount 退款金额
     * @param outRequestNo 标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传
     * @return
     * @throws AlipayApiException
     */
    @Override
    public String refund(String outTradeNo, String refundReason, Integer refundAmount, String outRequestNo) throws AlipayApiException {
        AlipayTradeRefundRequest alipayTradeRefundRequest = new AlipayTradeRefundRequest();

        alipayTradeRefundRequest.setBizContent("{\"out_trade_no\":\""+ outTradeNo +"\","
                + "\"refund_amount\":\""+ refundAmount +"\","
                + "\"refund_reason\":\""+ refundReason +"\","
                + "\"out_request_no\":\""+ outRequestNo +"\"}");
        String result = alipayClient.execute(alipayTradeRefundRequest).getBody();
        return result;
    }

    /**
     * 统一收单交易退款查询
     * @param outTradeNo 订单编号（唯一）
     * @param outRequestNo 标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传
     * @return
     * @throws AlipayApiException
     */
    @Override
    public String refundQuery(String outTradeNo, String outRequestNo) throws AlipayApiException {
        AlipayTradeFastpayRefundQueryRequest alipayTradeFastpayRefundQueryRequest = new AlipayTradeFastpayRefundQueryRequest();
        alipayTradeFastpayRefundQueryRequest.setBizContent("{\"out_trade_no\":\""+ outTradeNo +"\","
                +"\"out_request_no\":\""+ outRequestNo +"\"}");
        String result = alipayClient.execute(alipayTradeFastpayRefundQueryRequest).getBody();
        return result;
    }

    /**
     * 统一收单交易关闭接口
     * @param outTradeNo 订单编号（唯一）
     * @return
     * @throws AlipayApiException
     */
    @Override
    public String close(String outTradeNo) throws AlipayApiException {
        AlipayTradeCloseRequest alipayRequest = new AlipayTradeCloseRequest();
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ outTradeNo +"\"," +"\"trade_no\":\""+ "" +"\"}");
        String result = alipayClient.execute(alipayRequest).getBody();
        return result;
    }

}