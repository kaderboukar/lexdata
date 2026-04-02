package com.lexdata.auth.security.services;

import com.lexdata.auth.config.RabbitMQConfig;
import com.lexdata.auth.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Publication de l'événement UserRegisteredEvent pour l'utilisateur ID: {}", event.getId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_USER_CREATE, event);
    }
}
