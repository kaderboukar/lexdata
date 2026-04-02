package com.lexdata.notifications.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String NOTIFICATION_EXCHANGE = "lexdata.exchange.notifications";
    public static final String ADMIN_ALERTS_QUEUE = "lexdata.queue.admin-alerts";
    public static final String ADMIN_ALERTS_ROUTING_KEY = "notification.admin";

    public static final String SYNTHESE_PUBLISHED_QUEUE = "lexdata.queue.synthese-publiee";
    public static final String SYNTHESE_PUBLISHED_ROUTING_KEY = "notification.synthese.published";

    public static final String USER_ROLE_CHANGED_QUEUE = "lexdata.queue.user-role-changed";
    public static final String USER_ROLE_CHANGED_ROUTING_KEY = "notification.user.role.changed";

    // Dead Letter Queue constants
    public static final String DLX_NAME = "lexdata.exchange.dlx";
    public static final String DLQ_NAME = "lexdata.queue.notifications.dlq";
    public static final String DLQ_ROUTING_KEY = "notifications.dead";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue adminAlertsQueue() {
        return new Queue(ADMIN_ALERTS_QUEUE);
    }

    @Bean
    public Binding bindingAdminAlerts(Queue adminAlertsQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(adminAlertsQueue).to(notificationExchange).with(ADMIN_ALERTS_ROUTING_KEY);
    }

    @Bean
    public Queue synthesePublishedQueue() {
        return new Queue(SYNTHESE_PUBLISHED_QUEUE);
    }

    @Bean
    public Binding bindingSynthesePublished(Queue synthesePublishedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(synthesePublishedQueue).to(notificationExchange).with(SYNTHESE_PUBLISHED_ROUTING_KEY);
    }

    @Bean
    public Queue userRoleChangedQueue() {
        return new Queue(USER_ROLE_CHANGED_QUEUE);
    }

    @Bean
    public Binding bindingUserRoleChanged(Queue userRoleChangedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(userRoleChangedQueue).to(notificationExchange).with(USER_ROLE_CHANGED_ROUTING_KEY);
    }

    // --- DEAD LETTER QUEUE CONFIGURATION ---

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX_NAME);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ_NAME);
    }

    @Bean
    public Binding bindingDLQ(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DLQ_ROUTING_KEY);
    }
}
