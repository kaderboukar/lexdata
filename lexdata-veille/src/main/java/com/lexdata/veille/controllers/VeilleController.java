package com.lexdata.veille.controllers;

import com.lexdata.veille.dto.AlerteVeilleDto;
import com.lexdata.veille.dto.UserAlertDto;
import com.lexdata.veille.dto.VeilleSubscriptionDto;
import com.lexdata.veille.models.AlerteVeille;
import com.lexdata.veille.payload.AlerteRequest;
import com.lexdata.veille.payload.VeilleSubscriptionRequest;
import com.lexdata.veille.repository.AlerteVeilleRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lexdata.veille.util.VeilleDomainNormalizer;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/veille")
public class VeilleController {

    private final AlerteVeilleRepository alerteRepository;
    private final com.lexdata.veille.services.UserAlertService userAlertService;
    private final com.lexdata.veille.services.SubscriptionService subscriptionService;
    private final com.lexdata.veille.services.AlertePublicationService alertePublicationService;

    public VeilleController(AlerteVeilleRepository alerteRepository,
            com.lexdata.veille.services.UserAlertService userAlertService,
            com.lexdata.veille.services.SubscriptionService subscriptionService,
            com.lexdata.veille.services.AlertePublicationService alertePublicationService) {
        this.alerteRepository = alerteRepository;
        this.userAlertService = userAlertService;
        this.subscriptionService = subscriptionService;
        this.alertePublicationService = alertePublicationService;
    }

    // --- POUR LES AGENTS (GESTION) ---
    @GetMapping("/admin/alertes")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public Page<AlerteVeilleDto> getAllAlertes(@PageableDefault(size = 10) Pageable pageable) {
        return alerteRepository.findAll(pageable).map(this::mapToDto);
    }

    @PostMapping("/admin/alertes")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AlerteVeilleDto> createAlerte(@Valid @RequestBody AlerteRequest request) {
        AlerteVeille alerte = mapToEntity(request);
        AlerteVeille saved = alerteRepository.save(alerte);
        AlerteVeille full = alerteRepository.findByIdWithCollections(saved.getId()).orElseThrow();
        if (full.getStatus() == AlerteVeille.AlertStatus.PUBLISHED) {
            alertePublicationService.onFirstPublication(full);
        }
        return ResponseEntity.ok(mapToDto(full));
    }

    @PatchMapping("/admin/alertes/{id}/status")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AlerteVeilleDto> updateStatus(@PathVariable Long id,
            @RequestParam AlerteVeille.AlertStatus status) {
        return alerteRepository.findById(id)
                .map(alerte -> {
                    AlerteVeille.AlertStatus previous = alerte.getStatus();
                    alerte.setStatus(status);
                    AlerteVeille saved = alerteRepository.save(alerte);
                    AlerteVeille full = alerteRepository.findByIdWithCollections(saved.getId()).orElseThrow();

                    if (status == AlerteVeille.AlertStatus.PUBLISHED
                            && previous != AlerteVeille.AlertStatus.PUBLISHED) {
                        alertePublicationService.onFirstPublication(full);
                    }

                    return ResponseEntity.ok(mapToDto(full));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- ÉDITION (UPDATE) ---
    @PutMapping("/admin/alertes/{id}")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AlerteVeilleDto> updateAlerte(@PathVariable Long id, @Valid @RequestBody AlerteRequest request) {
        return alerteRepository.findById(id)
                .map(alerte -> {
                    AlerteVeille.AlertStatus previous = alerte.getStatus();
                    // Mise à jour des champs
                    alerte.setTitre(request.getTitre());
                    alerte.setTexteJuridiqueId(request.getTexteJuridiqueId());
                    alerte.setDatePublicationOfficielle(request.getDatePublicationOfficielle());
                    alerte.setDateEntreeEnVigueur(request.getDateEntreeEnVigueur());
                    alerte.setResumeClarifie(request.getResumeClarifie());
                    alerte.setPointsClesModifies(request.getPointsClesModifies());
                    alerte.setImpactsPratiques(request.getImpactsPratiques());
                    alerte.setConseilsConformite(request.getConseilsConformite());
                    alerte.setUrgence(AlerteVeille.UrgenceLevel.valueOf(
                            (request.getUrgence() != null ? request.getUrgence() : "BASSE").toUpperCase()
                    ));
                    alerte.setStatus(AlerteVeille.AlertStatus.valueOf(
                            (request.getStatus() != null ? request.getStatus() : "DRAFT").toUpperCase()
                    ));
                    alerte.setEventType(AlerteVeille.EventType.valueOf(
                            (request.getEventType() != null ? request.getEventType() : "NEW_TEXT").toUpperCase()
                    ));
                    alerte.setTexteType(request.getTexteType() == null ? null : request.getTexteType().toUpperCase());
                    alerte.setLienTexte(request.getLienTexte());
                    alerte.setLienSynthese(request.getLienSynthese());
                    alerte.setDomainesCibles(normalizeAlertDomains(request.getDomainesCibles()));
                    alerte.setSecteursImpactes(normalizeUpperSet(request.getSecteursImpactes()));

                    AlerteVeille updatedAlerte = alerteRepository.save(alerte);
                    AlerteVeille full = alerteRepository.findByIdWithCollections(updatedAlerte.getId())
                            .orElseThrow();
                    if (updatedAlerte.getStatus() == AlerteVeille.AlertStatus.PUBLISHED
                            && previous != AlerteVeille.AlertStatus.PUBLISHED) {
                        alertePublicationService.onFirstPublication(full);
                    }
                    return ResponseEntity.ok(mapToDto(full));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- SUPPRESSION (DELETE) ---
    @DeleteMapping("/admin/alertes/{id}")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteAlerte(@PathVariable Long id) {
        return alerteRepository.findById(id)
                .map(alerte -> {
                    // Note : Si vous préférez le Soft Delete comme pour les textes, 
                    // remplacez la ligne ci-dessous par : 
                    // alerte.setDeleted(true); alerteRepository.save(alerte);
                    alerteRepository.delete(alerte);

                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- POUR LES UTILISATEURS (CONSULTATION) ---
    @GetMapping("/feed")
    public ResponseEntity<List<AlerteVeilleDto>> getMyFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserAlertDto> feed = userAlertService.myAlerts(null, null, null, null, null,
                org.springframework.data.domain.PageRequest.of(page, size));
        return ResponseEntity.ok(feed.getContent().stream().map(ua -> AlerteVeilleDto.builder()
                .id(ua.getAlertId())
                .titre(ua.getTitle())
                .texteJuridiqueId(ua.getLegalTextId())
                .resumeClarifie(ua.getSummary())
                .dateCreation(ua.getAlertDate())
                .build()).collect(Collectors.toList()));
    }

    @GetMapping("/alertes/{id}")
    public ResponseEntity<AlerteVeilleDto> getAlerteById(@PathVariable Long id) {
        return alerteRepository.findByIdWithCollections(id)
                .filter(a -> a.getStatus() == AlerteVeille.AlertStatus.PUBLISHED)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<VeilleSubscriptionDto> createSubscription(@Valid @RequestBody VeilleSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.create(request));
    }

    @PutMapping("/subscriptions/{id}")
    public ResponseEntity<VeilleSubscriptionDto> updateSubscription(@PathVariable Long id,
                                                                    @Valid @RequestBody VeilleSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.update(id, request));
    }

    @PatchMapping("/subscriptions/{id}/active")
    public ResponseEntity<VeilleSubscriptionDto> toggleSubscription(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(subscriptionService.toggle(id, active));
    }

    @GetMapping("/subscriptions/me")
    public ResponseEntity<List<VeilleSubscriptionDto>> mySubscriptions() {
        return ResponseEntity.ok(subscriptionService.mySubscriptions());
    }

    @GetMapping("/alerts/me")
    public ResponseEntity<Page<UserAlertDto>> myAlerts(
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) String domaine,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userAlertService.myAlerts(unreadOnly, domaine, type, fromDate, toDate, pageable));
    }

    @PatchMapping("/alerts/me/{userAlertId}/read")
    public ResponseEntity<UserAlertDto> markRead(@PathVariable Long userAlertId, @RequestParam boolean read) {
        return ResponseEntity.ok(userAlertService.markAsRead(userAlertId, read));
    }

    @DeleteMapping("/alerts/me/{userAlertId}")
    public ResponseEntity<Void> deleteMyAlert(@PathVariable Long userAlertId) {
        userAlertService.softDelete(userAlertId);
        return ResponseEntity.noContent().build();
    }

    private AlerteVeille mapToEntity(AlerteRequest request) {
        Set<String> domaines = normalizeAlertDomains(request.getDomainesCibles());
        Set<String> secteurs = normalizeUpperSet(request.getSecteursImpactes());

        return AlerteVeille.builder()
                .titre(request.getTitre())
                .texteJuridiqueId(request.getTexteJuridiqueId())
                .eventType(AlerteVeille.EventType.valueOf(
                        request.getEventType() == null ? "NEW_TEXT" : request.getEventType().toUpperCase()))
                .texteType(request.getTexteType() == null ? null : request.getTexteType().toUpperCase())
                .datePublicationOfficielle(request.getDatePublicationOfficielle())
                .dateEntreeEnVigueur(request.getDateEntreeEnVigueur())
                .resumeClarifie(request.getResumeClarifie())
                .pointsClesModifies(request.getPointsClesModifies())
                .impactsPratiques(request.getImpactsPratiques())
                .conseilsConformite(request.getConseilsConformite())
                .urgence(AlerteVeille.UrgenceLevel
                        .valueOf((request.getUrgence() != null ? request.getUrgence() : "BASSE").toUpperCase()))
                .status(AlerteVeille.AlertStatus.valueOf(
                        request.getStatus() == null ? "DRAFT" : request.getStatus().toUpperCase()))
                .lienTexte(request.getLienTexte())
                .lienSynthese(request.getLienSynthese())
                .domainesCibles(domaines)
                .secteursImpactes(secteurs)
                .build();
    }

    private AlerteVeilleDto mapToDto(AlerteVeille alerte) {
        Set<String> domaines = alerte.getDomainesCibles() == null ? Set.of() : new HashSet<>(alerte.getDomainesCibles());
        Set<String> secteurs = alerte.getSecteursImpactes() == null ? Set.of() : new HashSet<>(alerte.getSecteursImpactes());

        return AlerteVeilleDto.builder()
                .id(alerte.getId())
                .titre(alerte.getTitre())
                .texteJuridiqueId(alerte.getTexteJuridiqueId())
                .datePublicationOfficielle(alerte.getDatePublicationOfficielle())
                .dateEntreeEnVigueur(alerte.getDateEntreeEnVigueur())
                .resumeClarifie(alerte.getResumeClarifie())
                .pointsClesModifies(alerte.getPointsClesModifies())
                .impactsPratiques(alerte.getImpactsPratiques())
                .conseilsConformite(alerte.getConseilsConformite())
                .eventType(alerte.getEventType().name())
                .texteType(alerte.getTexteType())
                .lienTexte(alerte.getLienTexte())
                .lienSynthese(alerte.getLienSynthese())
                .urgence(alerte.getUrgence().name())
                .statut(alerte.getStatus().name())
                .domainesCibles(domaines)
                .secteursImpactes(secteurs)
                .dateCreation(alerte.getDateCreation())
                .build();
    }

    private static Set<String> normalizeUpperSet(Set<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return new HashSet<>();
        }
        return raw.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toUpperCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(HashSet::new));
    }

    /** Domaines juridiques → codes canoniques (ex. FISCAL → DROIT_FISCAL). */
    private static Set<String> normalizeAlertDomains(Set<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return new HashSet<>();
        }
        return raw.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toUpperCase())
                .filter(s -> !s.isEmpty())
                .map(VeilleDomainNormalizer::canonical)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
