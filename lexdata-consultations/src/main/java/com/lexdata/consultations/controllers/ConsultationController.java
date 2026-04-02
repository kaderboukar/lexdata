package com.lexdata.consultations.controllers;

import com.lexdata.consultations.models.Consultation;
import com.lexdata.consultations.models.Formation;
import com.lexdata.consultations.models.CompanyCreationPack;
import com.lexdata.consultations.services.ConsultationService;
import com.lexdata.consultations.repository.ConsultationRepository;
import com.lexdata.consultations.repository.CompanyCreationPackRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService consultationService;
    private final ConsultationRepository consultationRepository;
    private final CompanyCreationPackRepository packRepository;

    public ConsultationController(ConsultationService consultationService, 
                                  ConsultationRepository consultationRepository, 
                                  CompanyCreationPackRepository packRepository) {
        this.consultationService = consultationService;
        this.consultationRepository = consultationRepository;
        this.packRepository = packRepository;
    }

    @GetMapping
    public List<Consultation> getMyConsultations() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return consultationRepository.findByClientUsername(username);
    }

    @PostMapping
    public ResponseEntity<Consultation> bookConsultation(@RequestBody Consultation consultation) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        consultation.setClientUsername(username);
        return ResponseEntity.ok(consultationService.reserverConsultation(consultation));
    }

    @GetMapping("/formations")
    public List<Formation> getFormations() {
        return consultationService.catalogueFormations();
    }

    @PostMapping("/packs")
    public ResponseEntity<CompanyCreationPack> startPack(@RequestBody CompanyCreationPack pack) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        pack.setOwnerUsername(username);
        return ResponseEntity.ok(consultationService.demarrerAccompagnement(pack));
    }

    @GetMapping("/packs")
    public List<CompanyCreationPack> getMyPacks() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return packRepository.findByOwnerUsername(username);
    }
}
