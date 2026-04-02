package com.lexdata.monitoring.services;

import com.lexdata.monitoring.models.AlertRule;
import com.lexdata.monitoring.models.TechnicalLog;
import com.lexdata.monitoring.repository.AlertRuleRepository;
import com.lexdata.monitoring.repository.TechnicalLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitoringService {
    private static final Logger logger = LoggerFactory.getLogger(MonitoringService.class);

    private final TechnicalLogRepository logRepository;
    private final AlertRuleRepository alertRuleRepository;

    public MonitoringService(TechnicalLogRepository logRepository, AlertRuleRepository alertRuleRepository) {
        this.logRepository = logRepository;
        this.alertRuleRepository = alertRuleRepository;
    }

    public void logError(TechnicalLog errorLog) {
        errorLog.setLevel("ERROR");
        logRepository.save(errorLog);
        logger.error("Erreur technique critique remontée par {}: {}", errorLog.getServiceName(), errorLog.getMessage());
        
        // Vérification immédiate si cette erreur doit déclencher une alerte fatale
        if (errorLog.getStatusCode() != null && errorLog.getStatusCode() >= 500) {
            triggerCriticalAlert("Erreur 5xx sur " + errorLog.getServiceName(), "L'endpoint " + errorLog.getEndpoint() + " renvoie des erreurs 500.");
        }
    }

    @Scheduled(fixedRate = 60000) // Toutes les minutes
    public void checkAlertRules() {
        List<AlertRule> rules = alertRuleRepository.findByEnabledTrue();
        for (AlertRule rule : rules) {
            // Simulation de la vérification des métriques via Prometheus ou Actuator (Spring Cloud)
            // En prod, on interrogerait le stockage de métriques
            logger.debug("Vérification de la règle d'alerte: {}", rule.getName());
            
            // Simulation d'un dépassement de seuil pour la démo
            if (rule.getName().contains("CRITIQUE") && Math.random() > 0.95) {
                triggerCriticalAlert("Alerte " + rule.getName(), "Le seuil de " + rule.getThreshold() + " a été dépassé pour " + rule.getMetricName());
            }
        }
    }

    private void triggerCriticalAlert(String title, String message) {
        // En prod, appellerait lexdata-notifications via un client Feign ou un bus d'événements
        logger.warn("ALERTE CRITIQUE PLATEFORME: {} - {}", title, message);
    }
}
