package com.jm.rabbitmq.controller;

import com.jm.rabbitmq.config.DelayedQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 发送延迟消息
 *
 * http://localhost:8080/ttl/sendMsg/嘻嘻嘻
 */
@Slf4j
@RestController
@RequestMapping("/ttl")
public class SendMsgController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //开始发消息 消息
    @GetMapping("/sendMsg/{message}")
    public void sendMsg(@PathVariable String message){
        log.info("当前时间：{}，发送一条消息给两个TTL队列：{}",new Date().toString(),message);

        rabbitTemplate.convertAndSend("X","XA","消息来自TTL为10s的队列:"+message);
        rabbitTemplate.convertAndSend("X","XB","消息来自TTL为40s的队列:"+message);
    }

    //开始发消息 消息 TTL
    @GetMapping("/sendExpirationMsg/{message}/{ttlTime}")
    public void sendMsg(@PathVariable String message,@PathVariable String ttlTime){
        log.info("当前时间：{}，发送一条时长{}毫秒TTL消息给队列QC：{}",
                new Date().toString(),ttlTime,message);

        rabbitTemplate.convertAndSend("X","XC",message,msg -> {
            //发送消息的时候，延迟时长
            msg.getMessageProperties().setExpiration(ttlTime);
            return msg;
        });
    }

    //开始发消息 基于插件的 消息 及 延迟的时间
    @GetMapping("/sendDelayMsg/{message}/{delayTime}")
    public void sendMsg(@PathVariable String message,@PathVariable Integer delayTime){
        log.info("当前时间：{}，发送一条时长{}毫秒消息给延迟队列delayed.queue：{}",
                new Date().toString(),delayTime,message);

        rabbitTemplate.convertAndSend(DelayedQueueConfig.DELAYED_EXCHANGE_NAME,
                DelayedQueueConfig.DELAYED_ROUTING_KEY,message, msg -> {
            //发送消息的时候，延迟时长 单位:ms
            msg.getMessageProperties().setDelay(delayTime);
            return msg;
        });
    }

}
