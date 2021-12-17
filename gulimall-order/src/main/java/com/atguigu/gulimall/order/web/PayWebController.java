package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.AlipayService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayWebController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private OrderService orderService;

    /**
     * 将支付页让浏览器显示
     * @param orderSn
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws Exception {
        PayVo payVo = orderService.getOrderPay(orderSn);
        String pay = alipayService.pay(orderSn, payVo.getTotal_amount(), payVo.getSubject());
        return pay;
    }
}
