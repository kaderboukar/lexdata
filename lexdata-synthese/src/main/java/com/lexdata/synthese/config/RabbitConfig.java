package com.lexdata.synthese.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String JURIDIQUE_EXCHANGE = "lexdata.exchange.juridique";
    public static final String TEXTE_MODIFIE_QUEUE = "lexdata.queue.texte-modifie";
    public static final String TEXTE_MODIFIE_ROUTING_KEY = "texte.modifie";

    @Bean
    public TopicExchange juridiqueExchange() {
        return new TopicExchange(JURIDIQUE_EXCHANGE);
    }

    @Bean
    public Queue texteModifieQueue() {
        return new Queue(TEXTE_MODIFIE_QUEUE);
    }

    @Bean
    public Binding bindingTexteModifie(Queue texteModifieQueue, TopicExchange juridiqueExchange) {
        return BindingBuilder.bind(texteModifieQueue).to(juridiqueExchange).with(TEXTE_MODIFIE_ROUTING_KEY);
    }
}
