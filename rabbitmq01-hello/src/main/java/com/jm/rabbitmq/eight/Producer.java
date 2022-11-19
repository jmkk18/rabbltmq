package com.jm.rabbitmq.eight;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

/**
 * 死信队列
 *
 * 生产者
 */
public class Producer {
    //普通交换机的名称
    public static final String NORMAL_EXCHANGE="normal_exchange";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //死信消息 设置TTL （time to live） 单位是ms  10s=10000ms
        /*AMQP.BasicProperties properties=
                new AMQP.BasicProperties()
                        .builder().expiration("10000").build();*/
        for (int i = 1; i < 11; i++) {
            String message="info"+i;
            channel.basicPublish(NORMAL_EXCHANGE,"zhangsan",null,message.getBytes("UTF-8"));
        }


    }
}
