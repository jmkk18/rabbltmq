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
 * 消费者01
 */
public class Consumer01 {
    //普通交换机的名称
    public static final String NORMAL_EXCHANGE="normal_exchange";
    //死信交换机的名称
    public static final String DEAD_EXCHANGE="dead_exchange";
    //普通队列的名称
    public static final String NORMAL_QUEUE="normal_queue";
    //死信队列的名称
    public static final String DEAD_QUEUE="dead_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //声明 普通和死信交换机 的类型为direct
        channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(DEAD_EXCHANGE, BuiltinExchangeType.DIRECT);

        //声明 普通队列
        Map<String, Object> arguments=new HashMap<>();
        //过期时间 10s=10000ms
        //arguments.put("x-message-ttl",10000);
        //正常队列设置死信交换机
        arguments.put("x-dead-letter-exchange",DEAD_EXCHANGE);
        //设置死信RoutingKey
        arguments.put("x-dead-letter-routing-key","lisi");
        //设置正常队列长度的限制
        //arguments.put("x-max-length",6);
        channel.queueDeclare(NORMAL_QUEUE,false,false,false,arguments);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //声明 死信队列
        channel.queueDeclare(DEAD_QUEUE,false,false,false,null);

        //绑定 普通交换机与普通队列
        channel.queueBind(NORMAL_QUEUE,NORMAL_EXCHANGE,"zhangsan");
        //绑定 死信交换机与死信队列
        channel.queueBind(DEAD_QUEUE,DEAD_EXCHANGE,"lisi");

        System.out.println("等待接收消息.....");

        DeliverCallback deliverCallback= (consumerTag,message)->{
            String msg=new String(message.getBody(),"UTF-8");
            if("info5".equals(msg)){
                System.out.println("Consumer01接收的消息："+msg+"：此消息是被C1拒绝的");
                channel.basicReject(message.getEnvelope().getDeliveryTag(),false);
            }else{
                System.out.println("Consumer01接收的消息："+msg);
                channel.basicAck(message.getEnvelope().getDeliveryTag(),false);
            }
        };

        //消费
        channel.basicConsume(NORMAL_QUEUE,false,deliverCallback,consumerTag -> {});
    }
}
