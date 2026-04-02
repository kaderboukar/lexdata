package com.lexdata.veille.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "lexdata.exchange.veille";
    public static final String QUEUE_ALERTE_PUBLIE = "lexdata.queue.alerte-publie";
    public static final String ROUTING_KEY_ALERTE_PUBLIE = "alerte.publie";
    
    // Ajout de la file d'attente manquante pour écouter les publications de textes
    public static final String QUEUE_TEXTE_PUBLIE = "lexdata.queue.texte-publie";
    public static final String ROUTING_KEY_TEXTE_PUBLIE = "texte.publie";
    public static final String QUEUE_TEXTE_MODIFIE = "lexdata.queue.texte-modifie";
    public static final String ROUTING_KEY_TEXTE_MODIFIE = "texte.modifie";

    @Bean
    public DirectExchange veilleExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue alertePublieQueue() {
        return new Queue(QUEUE_ALERTE_PUBLIE, true);
    }

    @Bean
    public Binding bindingAlertePublie() {
        return BindingBuilder
                .bind(alertePublieQueue())
                .to(veilleExchange())
                .with(ROUTING_KEY_ALERTE_PUBLIE);
    }

    @Bean
    public Queue textePublieQueue() {
        return new Queue(QUEUE_TEXTE_PUBLIE, true);
    }

    @Bean
    public Binding bindingTextePublie() {
        return BindingBuilder
                .bind(textePublieQueue())
                .to(veilleExchange())
                .with(ROUTING_KEY_TEXTE_PUBLIE);
    }

    @Bean
    public Queue texteModifieQueue() {
        return new Queue(QUEUE_TEXTE_MODIFIE, true);
    }

    @Bean
    public Binding bindingTexteModifie() {
        return BindingBuilder
                .bind(texteModifieQueue())
                .to(veilleExchange())
                .with(ROUTING_KEY_TEXTE_MODIFIE);
    }
}
