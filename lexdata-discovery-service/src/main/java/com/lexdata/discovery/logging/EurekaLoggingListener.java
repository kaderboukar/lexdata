package com.lexdata.discovery.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EurekaLoggingListener {

    private static final Logger log = LoggerFactory.getLogger(EurekaLoggingListener.class);

    @EventListener
    public void onApplicationEvent(EurekaInstanceRegisteredEvent event) {
        log.info(">>> SERVICE ENREGISTRÉ : {} - ID: {} - IP: {}:{}",
                event.getInstanceInfo().getAppName(),
                event.getInstanceInfo().getInstanceId(),
                event.getInstanceInfo().getIPAddr(),
                event.getInstanceInfo().getPort());
    }

    @EventListener
    public void onApplicationEvent(EurekaInstanceCanceledEvent event) {
        log.warn("<<< SERVICE DÉSINSCRIT : {} - ID: {}",
                event.getAppName(),
                event.getServerId());
    }

    @EventListener
    public void onApplicationEvent(EurekaInstanceRenewedEvent event) {
        log.debug("--- HEARTBEAT RECU : {} - ID: {}",
                event.getAppName(),
                event.getServerId());
    }
}
