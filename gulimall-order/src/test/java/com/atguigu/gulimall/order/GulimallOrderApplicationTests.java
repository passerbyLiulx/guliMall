package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessageTest() {
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setCreateTime(new Date());
        reasonEntity.setName("哈哈");
        reasonEntity.setSort(1);
        reasonEntity.setStatus(1);


        String msg = "Hello world!";
        rabbitTemplate.convertAndSend("hello-java-wxchange", "hello.java", reasonEntity);
        log.info("消息发送完成：{}", reasonEntity);
    }

    @Test
    public void createExchange() {

        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);

        amqpAdmin.declareExchange(directExchange);

        log.info("Exchange[{}]创建成功", directExchange.getName());
    }

    @Test
    public void createQueue() {
        Queue queue = new Queue("hello-java-queue", true, false,false);

        amqpAdmin.declareQueue(queue);

        log.info("Queue[{}]创建成功", queue.getName());
    }

    @Test
    public void createBinding() {
        Binding binding = new Binding("hello-java-binding",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java",
                null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功", "hello-java-binding");
    }





}
