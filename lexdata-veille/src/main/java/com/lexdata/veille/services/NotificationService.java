package com.lexdata.veille.services;

import com.lexdata.veille.models.AlerteVeille;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void dispatchAlerte(AlerteVeille alerte) {
        logger.info("DÉPÊCHE VEILLE: [ID={}] {}", alerte.getId(), alerte.getTitre());
        
        // Simulation d'envoi Email
        logger.info("Envoi EMAIL aux abonnés des domaines: {}", alerte.getDomainesCibles());
        
        // Simulation Push Notification
        if (alerte.getUrgence() == AlerteVeille.UrgenceLevel.ELEVEE) {
            logger.info("ALERTE PUSH MOBILE (URGENTE): {}", alerte.getTitre());
        }

        // Simulation SMS (Option Premium)
        if (alerte.getUrgence() == AlerteVeille.UrgenceLevel.ELEVEE) {
             logger.info("SMS PREMIUM: [LEXDATA] Alerte Juridique Importante: {}", alerte.getTitre());
        }
    }
}
