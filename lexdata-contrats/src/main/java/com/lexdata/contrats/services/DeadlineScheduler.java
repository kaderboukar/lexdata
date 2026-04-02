package com.lexdata.contrats.services;

import com.lexdata.contrats.models.Contract;
import com.lexdata.contrats.repository.ContractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DeadlineScheduler {
    private static final Logger logger = LoggerFactory.getLogger(DeadlineScheduler.class);
    private final ContractRepository contractRepository;

    public DeadlineScheduler(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Scheduled(cron = "0 0 8 * * *") // Chaque jour à 8h
    public void checkDeadlines() {
        logger.info("Vérification des échéances de contrats...");
        LocalDateTime threshold = LocalDateTime.now().plusDays(30);
        
        // Simuler la recherche de contrats dont l'échéance est proche
        // Note: Ici j'utilise ma méthode personnalisée dans le repository
        // findByStatusAndDateEcheanceBeforeAndRappelEnvoyeFalse
        
        // List<Contract> expiring = contractRepository.findByStatusAndDateEcheanceBeforeAndRappelEnvoyeFalse(Contract.ContractStatus.SIGNE, threshold);
        // expiring.forEach(c -> {
        //    logger.info("ALERTE: Contrat {} arrive à échéance le {}", c.getTitre(), c.getDateEcheance());
        //    c.setRappelEnvoye(true);
        //    contractRepository.save(c);
        //    // TODO: Appel notification-service
        // });
        
        logger.info("Vérification terminée.");
    }
}
