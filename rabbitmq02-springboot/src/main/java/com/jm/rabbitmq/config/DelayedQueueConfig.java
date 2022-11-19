package com.jm.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DelayedQueueConfig {
    //交换机
    public static final String DELAYED_EXCHANGE_NAME="delayed.exchange";
    //队列
    public static final String DELAYED_QUEUE_NAME="delayed.queue";
    //routingKey
    public static final String DELAYED_ROUTING_KEY="delayed.routingkey";

    //声明交换机 基于插件的
    @Bean
    public CustomExchange delayedExchange(){
        Map<String, Object> arguments=new HashMap<>();
        //自定义交换机的类型
        arguments.put("x-delayed-type","direct");
        /**
         * 1.交换机名称
         * 2.交换机的类型
         * 3.是否需要持久化
         * 4.是否需要自动删除
         * 5.其他参数
         */
        return new CustomExchange(DELAYED_EXCHANGE_NAME,"x-delayed-message",
                true,false,arguments);
    }

    //声明队列
    @Bean
    public Queue delayedQueue(){
        return new Queue(DELAYED_QUEUE_NAME);
    }

    //绑定交换机与队列
    @Bean
    public Binding delayedQueueBindingDelayedExchange(
            @Qualifier("delayedQueue") Queue delayedQueue,
            @Qualifier("delayedExchange") CustomExchange delayedExchange
    ){
        return BindingBuilder.bind(delayedQueue).to(delayedExchange).with(DELAYED_ROUTING_KEY).noargs();
    }


}
