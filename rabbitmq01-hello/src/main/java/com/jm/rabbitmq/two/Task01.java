package com.jm.rabbitmq.two;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.Channel;

import java.util.Queue;
import java.util.Scanner;

/**
 * 生产者 ：发送大量消息
 */
public class Task01 {
    //队列名称
    public static final String QUEUE_NAME="hello";

    //发送大量消息
    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        /**
         * 生成一个队列
         * 1.队列名称
         * 2.队列里的名称是否持久化(磁盘)，默认情况存储在内存中
         * 3.该队列是否提供一个消费者进行消费，是否进行消费共享，true可以多个消费者消费 false:只能一个消费者消费
         * 4.是否自动删除 最后一个消费者断开连接之后，该队列是否自动删除，true自动删除，false不自动删除
         * 5.其他参数
         */
        //声明队列
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //从控制台当中输入接收消息
        Scanner scanner=new Scanner(System.in);
        while (scanner.hasNext()){
            String message = scanner.next();
            /**
             * 发送一个消费
             * 1.发送到哪个交换机
             * 2.路由的Key值是哪个，本次队列的名称
             * 3.其他参数
             * 4.发送消息的消息体
             */
            channel.basicPublish("", QUEUE_NAME,null,message.getBytes());
            System.out.println("发送消息完成："+message);
        }

    }
}
