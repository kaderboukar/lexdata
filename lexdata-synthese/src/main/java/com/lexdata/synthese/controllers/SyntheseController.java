package com.lexdata.synthese.controllers;

import com.lexdata.synthese.dto.FicheSynthetiqueDto;
import com.lexdata.synthese.dto.SyntheseVersionDto;
import com.lexdata.synthese.models.FicheSynthetique;
import com.lexdata.synthese.models.SyntheseVersion;
import com.lexdata.synthese.payload.SyntheseRequest;
import com.lexdata.synthese.repository.FicheSynthetiqueRepository;
import com.lexdata.synthese.repository.SyntheseVersionRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.lexdata.synthese.services.ExportService;
import com.lexdata.synthese.services.LegalTextValidationService;

import java.util.List;

@RestController
@RequestMapping("/api/synthese")
public class SyntheseController {

    private final FicheSynthetiqueRepository ficheRepository;
    private final SyntheseVersionRepository versionRepository;
    private final ExportService exportService;
    private final LegalTextValidationService legalTextValidationService;

    public SyntheseController(FicheSynthetiqueRepository ficheRepository,
            SyntheseVersionRepository versionRepository,
            ExportService exportService,
            LegalTextValidationService legalTextValidationService) {
        this.ficheRepository = ficheRepository;
        this.versionRepository = versionRepository;
        this.exportService = exportService;
        this.legalTextValidationService = legalTextValidationService;
    }

    // --- AGENTS ---

    @PostMapping("/admin/fiches")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FicheSynthetiqueDto> createFiche(@Valid @RequestBody SyntheseRequest request) {
        validateLegalTextExists(request.getTexteJuridiqueId());
        FicheSynthetique fiche = mapToEntity(request);
        archiveExistingPublishedIfNeeded(fiche);
        FicheSynthetique saved = ficheRepository.save(fiche);
        saveVersion(saved, request.getCommentaireVersion(), request.getTexteJuridiqueVersionId());
        return ResponseEntity.ok(mapToDto(saved));
    }

    @PutMapping("/admin/fiches/{id}")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FicheSynthetiqueDto> updateFiche(@PathVariable Long id,
            @Valid @RequestBody SyntheseRequest request) {
        validateLegalTextExists(request.getTexteJuridiqueId());
        return ficheRepository.findById(id)
                .map(fiche -> {
                    updateEntity(fiche, request);
                    archiveExistingPublishedIfNeeded(fiche);
                    FicheSynthetique saved = ficheRepository.save(fiche);
                    saveVersion(saved, request.getCommentaireVersion(), request.getTexteJuridiqueVersionId());
                    return ResponseEntity.ok(mapToDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/admin/fiches/{id}/status")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FicheSynthetiqueDto> updateStatus(@PathVariable Long id,
            @RequestParam FicheSynthetique.SyntheseStatus status) {
        return ficheRepository.findById(id)
                .map(fiche -> {
                    fiche.setStatus(status);
                    archiveExistingPublishedIfNeeded(fiche);
                    return ResponseEntity.ok(mapToDto(ficheRepository.save(fiche)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/fiches/{id}")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteFiche(@PathVariable Long id) {
        if (!ficheRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ficheRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- PUBLIC / PREMIUM ---

    @GetMapping("/fiches")
    @PreAuthorize("isAuthenticated()")
    public Page<FicheSynthetiqueDto> getPublishedFiches(@PageableDefault(size = 10) Pageable pageable) {
        return ficheRepository.findByStatus(FicheSynthetique.SyntheseStatus.PUBLISHED, pageable).map(this::mapToDto);
    }

    @GetMapping("/fiches/{id}/pdf")
    @PreAuthorize("hasAnyRole('USER', 'JURISTE', 'AVOCAT', 'AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        return ficheRepository.findById(id)
                .filter(f -> f.getStatus() == FicheSynthetique.SyntheseStatus.PUBLISHED || isAdmin())
                .map(fiche -> {
                    byte[] pdf = exportService.generateFichePdf(fiche);
                    return ResponseEntity.ok()
                            .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"fiche_" + id + ".pdf\"")
                            .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                            .body(pdf);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fiches/{id}")
    @PreAuthorize("hasAnyRole('USER', 'JURISTE', 'AVOCAT', 'AGENT_ADMIN', 'SUPER_ADMIN')") // Premium logic can be added
                                                                                           // here
    public ResponseEntity<FicheSynthetiqueDto> getFicheById(@PathVariable Long id) {
        return ficheRepository.findById(id)
                .filter(f -> f.getStatus() == FicheSynthetique.SyntheseStatus.PUBLISHED || isAdmin())
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fiches/texte/{texteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FicheSynthetiqueDto> getPublishedByTexteId(@PathVariable Long texteId) {
        List<FicheSynthetique> published = ficheRepository.findPublishedByTexteJuridiqueId(texteId);
        if (published.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapToDto(published.get(0)));
    }

    @GetMapping("/admin/fiches/{id}/versions")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public List<SyntheseVersionDto> getVersions(@PathVariable Long id) {
        return versionRepository.findByFicheIdOrderByDateVersionDesc(id).stream().map(this::mapVersionToDto).toList();
    }

    private void saveVersion(FicheSynthetique fiche, String commentaire, Long texteVersionId) {
        String agentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        SyntheseVersion version = SyntheseVersion.builder()
                .fiche(fiche)
                .agentEmail(agentEmail)
                .commentaireVersion(commentaire != null ? commentaire : "Mise à jour standard")
                .contenuJson(fiche.toString()) // Simplifié pour la démo
                .texteJuridiqueVersionId(texteVersionId)
                .build();
        versionRepository.save(version);
    }

    private FicheSynthetique mapToEntity(SyntheseRequest request) {
        FicheSynthetique fiche = new FicheSynthetique();
        updateEntity(fiche, request);
        return fiche;
    }

    private void updateEntity(FicheSynthetique fiche, SyntheseRequest request) {
        fiche.setTitre(request.getTitre());
        fiche.setTexteJuridiqueId(request.getTexteJuridiqueId());
        fiche.setContent(request.getContent());
        fiche.setVersion(request.getVersion());
        fiche.setStatus(FicheSynthetique.SyntheseStatus.valueOf(request.getStatus().toUpperCase()));
        fiche.setObjectifPrincipal(request.getObjectifPrincipal());
        fiche.setChangementsCles(request.getChangementsCles());
        fiche.setObligations(request.getObligations());
        fiche.setSanctions(request.getSanctions());
        fiche.setConseilsPratiques(request.getConseilsPratiques());
        fiche.setExemplesConcrets(request.getExemplesConcrets());
    }

    private FicheSynthetiqueDto mapToDto(FicheSynthetique fiche) {
        return FicheSynthetiqueDto.builder()
                .id(fiche.getId())
                .titre(fiche.getTitre())
                .texteJuridiqueId(fiche.getTexteJuridiqueId())
                .content(fiche.getContent())
                .version(fiche.getVersion())
                .objectifPrincipal(fiche.getObjectifPrincipal())
                .changementsCles(fiche.getChangementsCles())
                .obligations(fiche.getObligations())
                .sanctions(fiche.getSanctions())
                .conseilsPratiques(fiche.getConseilsPratiques())
                .exemplesConcrets(fiche.getExemplesConcrets())
                .status(fiche.getStatus().name())
                .dateCreation(fiche.getDateCreation())
                .dateModification(fiche.getDateModification())
                .build();
    }

    private SyntheseVersionDto mapVersionToDto(SyntheseVersion version) {
        return SyntheseVersionDto.builder()
                .id(version.getId())
                .ficheId(version.getFiche().getId())
                .contenuJson(version.getContenuJson())
                .agentEmail(version.getAgentEmail())
                .commentaireVersion(version.getCommentaireVersion())
                .texteJuridiqueVersionId(version.getTexteJuridiqueVersionId())
                .dateVersion(version.getDateVersion())
                .build();
    }

    private void archiveExistingPublishedIfNeeded(FicheSynthetique fiche) {
        if (fiche.getStatus() != FicheSynthetique.SyntheseStatus.PUBLISHED) {
            return;
        }
        List<FicheSynthetique> published = ficheRepository.findByTexteJuridiqueIdAndStatusOrderByVersionDesc(
                fiche.getTexteJuridiqueId(),
                FicheSynthetique.SyntheseStatus.PUBLISHED);
        for (FicheSynthetique existing : published) {
            if (fiche.getId() == null || !existing.getId().equals(fiche.getId())) {
                existing.setStatus(FicheSynthetique.SyntheseStatus.ARCHIVED);
                ficheRepository.save(existing);
            }
        }
    }

    private void validateLegalTextExists(Long legalTextId) {
        if (!legalTextValidationService.exists(legalTextId)) {
            throw new IllegalArgumentException("legalTextId invalide: texte juridique introuvable.");
        }
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> "ROLE_AGENT_ADMIN".equals(a.getAuthority()) || "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
    }
}
