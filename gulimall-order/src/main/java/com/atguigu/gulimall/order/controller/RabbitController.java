package com.atguigu.gulimall.order.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RabbitController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public String sendMq(Integer num) {
        return null;
    }

}
