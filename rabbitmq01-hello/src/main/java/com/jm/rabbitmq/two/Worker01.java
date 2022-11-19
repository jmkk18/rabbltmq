package com.jm.rabbitmq.two;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * 这是一个工作线程（相当于消费者）
 */
public class Worker01 {
    //对立的名称
    public static final String QUEUE_NAME="hello";

    //接收消息
    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();

        //消息的接收
        DeliverCallback deliverCallback= (consumerTag,message) -> {
            System.out.println("接收到的消息："+new String(message.getBody()));
        };

        //消息接收被取消时，执行下面的内容
        CancelCallback cancelCallback= (consumerTag) -> {
            System.out.println(consumerTag+"->消息消费被中断");
        };
        /**
         * 消费者 接收消息
         * 1.消费哪个队列
         * 2.消费成功之后是否要自动应答 true 代表的是自动应答 false 代表手动应答
         * 3.消费者未成功消费的回调
         * 4.消费者取消消费的回调
         */
        System.out.println("C2等待接收消息......");
        channel.basicConsume(QUEUE_NAME,true,deliverCallback,cancelCallback);
    }
}
