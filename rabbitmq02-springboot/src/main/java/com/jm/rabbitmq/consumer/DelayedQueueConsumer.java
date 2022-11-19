package com.jm.rabbitmq.consumer;

import com.jm.rabbitmq.config.DelayedQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 消费者 基于插件的延迟消息
 */
@Slf4j
@Component
public class DelayedQueueConsumer {

    //监听消息
    @RabbitListener(queues = DelayedQueueConfig.DELAYED_QUEUE_NAME)
    public void receiveDelayQueueMessage(Message message) throws Exception {
        String msg=new String(message.getBody(),"UTF-8");
        log.info("当前时间：{}，收到延迟队列的消息：{}",new Date().toString(),msg);
    }

}
