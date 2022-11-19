package com.jm.rabbitmq.seven;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;


public class ReceiveLogsTopic01 {
    //交换机的名称
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        //声明交换机和队列的绑定
        String queueName="Q1";
        channel.queueDeclare(queueName,false,false,false,null);
        channel.queueBind(queueName,EXCHANGE_NAME,"*.orange.*");

        System.out.println("等待接收消息...");
       //接收消息
        DeliverCallback deliverCallback= (consumerTag, message)->{
            System.out.println("接收队列："+queueName+" 绑定键："+message.getEnvelope().getRoutingKey()+
                    " 消息："+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(queueName,true,deliverCallback,consumerTag -> {});
    }
}
