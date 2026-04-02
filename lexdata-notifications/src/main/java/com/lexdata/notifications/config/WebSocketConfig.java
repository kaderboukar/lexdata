package com.lexdata.notifications.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Active un broker simple en mémoire pour les destinations préfixées par /topic
        // et /queue
        config.enableSimpleBroker("/topic", "/queue");
        // Préfixe pour les messages envoyés DEPUIS le client VERS le serveur
        config.setApplicationDestinationPrefixes("/app");
        // Préfixe pour les destinations spécifiques à l'utilisateur
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint pour la connexion WebSocket, avec support SockJS pour la
        // compatibilité
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
