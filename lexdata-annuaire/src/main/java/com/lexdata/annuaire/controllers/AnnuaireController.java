package com.lexdata.annuaire.controllers;

import com.lexdata.annuaire.dto.ProfessionalProfileDto;
import com.lexdata.annuaire.models.Lead;
import com.lexdata.annuaire.models.ProfessionalProfile;
import com.lexdata.annuaire.payload.LeadRequest;
import com.lexdata.annuaire.payload.ProfileRequest;
import com.lexdata.annuaire.repository.LeadRepository;
import com.lexdata.annuaire.repository.ProfessionalProfileRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annuaire")
public class AnnuaireController {

    private final ProfessionalProfileRepository profileRepository;
    private final LeadRepository leadRepository;

    public AnnuaireController(ProfessionalProfileRepository profileRepository, LeadRepository leadRepository) {
        this.profileRepository = profileRepository;
        this.leadRepository = leadRepository;
    }

    // --- RECHERCHE PUBLIQUE ---

    @GetMapping("/search")
    public Page<ProfessionalProfileDto> search(
            @RequestParam(required = false) String expertise,
            @RequestParam(required = false) String ville,
            @PageableDefault(size = 10) Pageable pageable) {
        return profileRepository.searchProfiles(expertise, ville, pageable).map(this::mapToDto);
    }

    @GetMapping("/profiles/{id}")
    public ResponseEntity<ProfessionalProfileDto> getProfile(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- GESTION PROFIL (PROFESSIONNEL) ---

    @PostMapping("/my-profile")
    @PreAuthorize("hasAnyRole('AVOCAT', 'JURISTE', 'AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProfessionalProfileDto> createOrUpdateMyProfile(@Valid @RequestBody ProfileRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        ProfessionalProfile profile = profileRepository.findByUsername(username)
                .orElse(new ProfessionalProfile());
        
        profile.setUsername(username);
        updateProfileFields(profile, request);
        
        // Un changement majeur remet en attente de validation
        profile.setStatut(ProfessionalProfile.ValidationStatus.EN_ATTENTE);
        
        return ResponseEntity.ok(mapToDto(profileRepository.save(profile)));
    }

    @GetMapping("/my-profile")
    @PreAuthorize("hasAnyRole('AVOCAT', 'JURISTE', 'AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProfessionalProfileDto> getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return profileRepository.findByUsername(username)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- MISE EN RELATION (UTILISATEUR) ---

    @PostMapping("/profiles/{id}/contact")
    public ResponseEntity<String> contactPro(@PathVariable Long id, @Valid @RequestBody LeadRequest request) {
        String requester = SecurityContextHolder.getContext().getAuthentication().getName();
        return profileRepository.findById(id)
                .map(pro -> {
                    Lead lead = Lead.builder()
                            .professional(pro)
                            .requesterUsername(requester)
                            .objet(request.getObjet())
                            .descriptionBesoin(request.getDescriptionBesoin())
                            .build();
                    leadRepository.save(lead);
                    // TODO: Simulation d'envoi notification push/email au Pro
                    return ResponseEntity.ok("Demande de mise en relation envoyée avec succès.");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- MODERATION (AGENTS/ADMIN) ---

    @PatchMapping("/admin/profiles/{id}/validate")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProfessionalProfileDto> validateProfile(
            @PathVariable Long id, 
            @RequestParam ProfessionalProfile.ValidationStatus status) {
        return profileRepository.findById(id)
                .map(profile -> {
                    profile.setStatut(status);
                    if (status == ProfessionalProfile.ValidationStatus.VALIDE) {
                        profile.getBadges().add("VÉRIFIÉ PAR LEXDATA");
                    }
                    return ResponseEntity.ok(mapToDto(profileRepository.save(profile)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/profiles/pending")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public Page<ProfessionalProfileDto> getPendingProfiles(@PageableDefault(size = 10) Pageable pageable) {
        return profileRepository.findByStatut(ProfessionalProfile.ValidationStatus.EN_ATTENTE, pageable).map(this::mapToDto);
    }

    private void updateProfileFields(ProfessionalProfile profile, ProfileRequest request) {
        profile.setTitre(request.getTitre());
        profile.setCabinet(request.getCabinet());
        profile.setAdresse(request.getAdresse());
        profile.setVille(request.getVille());
        profile.setTelephone(request.getTelephone());
        profile.setEmail(request.getEmail());
        profile.setSiteWeb(request.getSiteWeb());
        profile.setNumeroOrdre(request.getNumeroOrdre());
        profile.setExpertises(request.getExpertises());
        profile.setLangues(request.getLangues());
        profile.setBio(request.getBio());
        profile.setTarifsIndicatifs(request.getTarifsIndicatifs());
        profile.setHorairesConsultation(request.getHorairesConsultation());
    }

    private ProfessionalProfileDto mapToDto(ProfessionalProfile profile) {
        return ProfessionalProfileDto.builder()
                .id(profile.getId())
                .username(profile.getUsername())
                .photoUrl(profile.getPhotoUrl())
                .titre(profile.getTitre())
                .cabinet(profile.getCabinet())
                .adresse(profile.getAdresse())
                .ville(profile.getVille())
                .telephone(profile.getTelephone())
                .email(profile.getEmail())
                .siteWeb(profile.getSiteWeb())
                .numeroOrdre(profile.getNumeroOrdre())
                .expertises(profile.getExpertises())
                .langues(profile.getLangues())
                .bio(profile.getBio())
                .tarifsIndicatifs(profile.getTarifsIndicatifs())
                .horairesConsultation(profile.getHorairesConsultation())
                .statut(profile.getStatut().name())
                .badges(profile.getBadges())
                .noteMoyenne(profile.getNoteMoyenne())
                .nombreAvis(profile.getNombreAvis())
                .build();
    }
}
