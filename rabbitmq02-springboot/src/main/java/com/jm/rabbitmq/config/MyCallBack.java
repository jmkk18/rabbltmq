package com.jm.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class MyCallBack implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * @PostConstruct 解释：
     *    @PostConstruct该注解被用来修饰一个非静态的void（）方法。
     *    被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器执行一次。
     *    PostConstruct在构造函数之后执行，init（）方法之前执行。
     */
    @PostConstruct
    public void init(){
        //注入
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    /**
     * 交换机确认回调方法
     * 1.发消息 交换机接收成功了 回调
     *      1.1 correlationData 保存回调消息的id和相关信息
     *      1.2 ack=true 交换机接收到消息了
     *      1.3 cause null
     * 2.发消息 交换机接受失败了 回调
     *      2.1 correlationData 保存回调消息的id和相关信息
     *      2.2 ack=false 交换机没有收到消息了
     *      2.3 cause 失败的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String id = correlationData.getId() != null ? correlationData.getId() : "";
        if(ack){
            log.info("交换机已经收到ID为：{}的消息",id);
        }else{
            log.info("交换机还未收到ID为：{}的消息，由于原因：{}",id,cause);
        }
    }

    //当消息无法路由的时候的回调方法
    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        log.info("消息{}，被交换机{}给回退了，退回的原因：{}，路由key：{}",
                new String(returnedMessage.getMessage().getBody()),
                returnedMessage.getExchange(),returnedMessage.getReplyText(),
                returnedMessage.getRoutingKey());
    }
}
