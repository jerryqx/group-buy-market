package com.qx.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventPublisher {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.config.producer.exchange}")
    private String exchangeName;


    public void publish(String routingKey, Object message) {
        log.info("[publish] routingKey: {}, message: {}", routingKey, message);
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message, message1 -> {
                // 持久化消息配置
                message1.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return message1;
            });
        } catch (Exception e) {
            log.error("发送MQ消息失败 team_success message:{}", message, e);
            throw e;
        }

    }
}




