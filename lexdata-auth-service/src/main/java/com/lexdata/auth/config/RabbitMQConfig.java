package com.lexdata.auth.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "lexdata.exchange";
    public static final String QUEUE_USER_PROFIL_CREATED = "user.profil.created.queue";
    public static final String ROUTING_KEY_USER_CREATE = "user.profil.create";

    public static final String QUEUE_USER_DELETED = "user.deleted.queue";
    public static final String ROUTING_KEY_USER_DELETE = "user.delete";

    @Bean
    public Queue userProfileQueue() {
        // durable = true (survit au redémarrage de RabbitMQ)
        return new Queue(QUEUE_USER_PROFIL_CREATED, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding bindingUserCreate(Queue userProfileQueue, DirectExchange exchange) {
        return BindingBuilder.bind(userProfileQueue).to(exchange).with(ROUTING_KEY_USER_CREATE);
    }

    @Bean
    public Queue userDeletedQueue() {
        return new Queue(QUEUE_USER_DELETED, true);
    }

    @Bean
    public Binding bindingUserDelete(Queue userDeletedQueue, DirectExchange exchange) {
        return BindingBuilder.bind(userDeletedQueue).to(exchange).with(ROUTING_KEY_USER_DELETE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
