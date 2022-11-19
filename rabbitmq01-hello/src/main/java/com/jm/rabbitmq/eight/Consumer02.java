package com.jm.rabbitmq.eight;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * 死信队列
 *
 * 消费者02
 */
public class Consumer02 {
    //死信队列的名称
    public static final String DEAD_QUEUE="dead_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();

        System.out.println("等待接收消息.....");

        DeliverCallback deliverCallback= (consumerTag,message)->{
            System.out.println("Consumer02接收的消息："+new String(message.getBody(),"UTF-8"));
        };

        //消费
        channel.basicConsume(DEAD_QUEUE,true,deliverCallback,consumerTag -> {});
    }
}
