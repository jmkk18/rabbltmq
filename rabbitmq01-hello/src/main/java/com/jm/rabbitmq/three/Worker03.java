package com.jm.rabbitmq.three;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.jm.rabbitmq.util.SleepUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * 消息在手动应答时不丢失、放回队列中重新消费
 */
public class Worker03 {
    //队列名称
    public static final String TASK_QUEUE_NAME="ack_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        System.out.println("C1等待接收消息处理时间较短");

        DeliverCallback deliverCallback=(consumerTag,message)->{
            //沉睡1s
            SleepUtils.sleep(1);
            System.out.println("接收到的消息："+new String(message.getBody(),"UTF-8"));
            //手动应答
            /**
             * 1.消息的标记 tag
             * 2.是否批量应答 false：不批量应答信道中的消息 true：批量
             */
            channel.basicAck(message.getEnvelope().getDeliveryTag(),false);
        };
        //设置不公平分发
        //channel.basicQos(1);
        //设置预取值2
        channel.basicQos(2);
        //采用手动应答
        channel.basicConsume(TASK_QUEUE_NAME,false,deliverCallback,consumerTag -> {
            System.out.println(consumerTag+"消费者取消消费接口回调逻辑");
        });
    }
}
