package com.lexdata.consultations.services;

import com.lexdata.consultations.models.Consultation;
import com.lexdata.consultations.models.CompanyCreationPack;
import com.lexdata.consultations.models.Formation;
import com.lexdata.consultations.repository.ConsultationRepository;
import com.lexdata.consultations.repository.FormationRepository;
import com.lexdata.consultations.repository.CompanyCreationPackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final FormationRepository formationRepository;
    private final CompanyCreationPackRepository packRepository;

    public ConsultationService(ConsultationRepository consultationRepository, 
                               FormationRepository formationRepository, 
                               CompanyCreationPackRepository packRepository) {
        this.consultationRepository = consultationRepository;
        this.formationRepository = formationRepository;
        this.packRepository = packRepository;
    }

    public Consultation reserverConsultation(Consultation consultation) {
        consultation.setStatus(Consultation.ConsultationStatus.EN_ATTENTE);
        // Simulation d'assignation automatique à un juriste disponible
        consultation.setJuristeUsername("expert_lexdata_1");
        return consultationRepository.save(consultation);
    }

    public List<Formation> catalogueFormations() {
        return formationRepository.findAll();
    }

    public CompanyCreationPack demarrerAccompagnement(CompanyCreationPack pack) {
        pack.setStatus(CompanyCreationPack.PackStatus.EN_COURS);
        pack.setChecklistStatus("RCCM: A faire, NIF: A faire, STATUTS: En cours");
        pack.setAssignedJuriste("expert_creation_2");
        return packRepository.save(pack);
    }
}
