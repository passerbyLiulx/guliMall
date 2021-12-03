package com.atguigu.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1.引入amqp场景，rabbitAutoConfiguration 就会自动生效
 * 2.给容器自动配置
 *   RabbitTemplate, AmqpAdmin, CachingConnectionFactory, RabbitMessagingTemplate
 *   所有的属性都是 spring.rabbitmq
 * 3.给配置文件配置 spring.rabbitmq 信息
 * 4.@EnableRabbit  开启功能
 * 5.监听消息  使用@RabbitListener时必须有@EnableRabbit
 *   @RabbitListener: 类+方法上
 *   @RabbitHandler: 标在方法上  重载区分不同的消息
 */
@EnableRabbit
@MapperScan("com.atguigu.gulimall.order.dao")
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
