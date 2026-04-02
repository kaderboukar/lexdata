package com.lexdata.user.services;

import com.lexdata.user.events.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeletionPublisher {

    private final RabbitTemplate rabbitTemplate;

    // On réutilise l'exchange commun
    public static final String EXCHANGE_NAME = "lexdata.exchange";
    public static final String ROUTING_KEY_USER_DELETE = "user.delete";

    public void publishUserDeletedEvent(String username) {
        log.info("Publication de l'événement UserDeletedEvent pour l'utilisateur: {}", username);
        UserDeletedEvent event = UserDeletedEvent.builder().username(username).build();
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY_USER_DELETE, event);
    }
}
