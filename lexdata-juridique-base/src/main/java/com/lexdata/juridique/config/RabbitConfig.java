package com.lexdata.juridique.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String JURIDIQUE_EXCHANGE = "lexdata.exchange.juridique";
    public static final String TEXTE_PUBLIE_QUEUE = "lexdata.queue.texte-publie";
    public static final String TEXTE_PUBLIE_ROUTING_KEY = "texte.publie";
    public static final String TEXTE_MODIFIE_ROUTING_KEY = "texte.modifie";

    @Bean
    public TopicExchange juridiqueExchange() {
        return new TopicExchange(JURIDIQUE_EXCHANGE);
    }

    @Bean
    public Queue textePublieQueue() {
        return new Queue(TEXTE_PUBLIE_QUEUE);
    }

    @Bean
    public Binding binding(Queue textePublieQueue, TopicExchange juridiqueExchange) {
        return BindingBuilder.bind(textePublieQueue).to(juridiqueExchange).with(TEXTE_PUBLIE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
