package com.atguigu.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gulimall.order.config.AlipayConfig;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class OrderPayedListener {

    @Autowired
    private OrderService orderService;

    @PostMapping("/payed/notify")
    public String handleAlipayed(PayAsyncVo payAsyncVo, HttpServletRequest request) throws AlipayApiException {
        // 验签
        Map<String, String[]> param = request.getParameterMap();
        boolean signVerified = AlipaySignature.rsaCheckV1((Map<String, String>) param, AlipayConfig.ALIPAY_PUBLIC_KEY,
                AlipayConfig.CHARSET, AlipayConfig.SIGN_TYPE);
        if (signVerified) {
            System.out.println("签名验证成功");
            String result = orderService.handlePayResult(payAsyncVo);
            return result;
        } else {
            return "error";
        }
    }
}
