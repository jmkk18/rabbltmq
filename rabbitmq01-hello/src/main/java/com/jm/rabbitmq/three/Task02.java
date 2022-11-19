package com.jm.rabbitmq.three;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import java.util.Scanner;

/**
 * 消息在手动应答时不丢失、放回队列中重新消费
 */
public class Task02 {
    //队列名称
    public static final String TASK_QUEUE_NAME="ack_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //声明队列
        boolean durable=true;//需要让队列持久化
        channel.queueDeclare(TASK_QUEUE_NAME,durable,false,false,null);
        //从控制台输入消息
        Scanner scanner=new Scanner(System.in);
        while(scanner.hasNext()){
            String message = scanner.next();
            channel.basicPublish("",TASK_QUEUE_NAME,MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes("UTF-8"));
            System.out.println("消费者发送消息成功："+message);
        }
    }
}
