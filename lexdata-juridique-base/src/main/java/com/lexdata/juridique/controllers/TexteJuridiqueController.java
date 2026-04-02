package com.lexdata.juridique.controllers;

import com.lexdata.juridique.dto.LegalAnnotationDto;
import com.lexdata.juridique.dto.TextVersionDto;
import com.lexdata.juridique.dto.TexteJuridiqueDto;
import com.lexdata.juridique.models.LegalAnnotation;
import com.lexdata.juridique.models.LegalDomain;
import com.lexdata.juridique.models.TextVersion;
import com.lexdata.juridique.models.TexteJuridique;
import com.lexdata.juridique.models.TypeTexte;
import com.lexdata.juridique.payload.AnnotationRequest;
import com.lexdata.juridique.payload.TexteRequest;
import com.lexdata.juridique.repository.LegalAnnotationRepository;
import com.lexdata.juridique.repository.TextVersionRepository;
import com.lexdata.juridique.repository.TexteJuridiqueRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/juridique/textes")
@Validated
public class TexteJuridiqueController {

    private final TexteJuridiqueRepository texteRepository;
    private final TextVersionRepository versionRepository;
    private final LegalAnnotationRepository annotationRepository;
    private final com.lexdata.juridique.services.TexteSearchService searchService;
    private final com.lexdata.juridique.events.JuridiqueEventPublisher eventPublisher;

    public TexteJuridiqueController(TexteJuridiqueRepository texteRepository,
            TextVersionRepository versionRepository,
            LegalAnnotationRepository annotationRepository,
            com.lexdata.juridique.services.TexteSearchService searchService,
            com.lexdata.juridique.events.JuridiqueEventPublisher eventPublisher) {
        this.texteRepository = texteRepository;
        this.versionRepository = versionRepository;
        this.annotationRepository = annotationRepository;
        this.searchService = searchService;
        this.eventPublisher = eventPublisher;
    }

    // --- RECHERCHE ET LECTURE ---
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<TexteJuridiqueDto> searchTextes(
            @RequestParam(name = "recherche", required = false) @Size(max = 200, message = "Le terme de recherche est trop long") String recherche,
            @RequestParam(name = "domaine", required = false) String domaine,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "includeNonPublie", defaultValue = "false") boolean includeNonPublie,
            @PageableDefault(size = 10, sort = "dateSignature", direction = Sort.Direction.DESC) Pageable pageable) {

        LegalDomain legalDomain = (domaine != null && !domaine.trim().isEmpty())
                ? LegalDomain.valueOf(domaine)
                : null;

        TypeTexte typeTexte = (type != null && !type.trim().isEmpty())
                ? TypeTexte.valueOf(type)
                : null;

        String searchPattern = (recherche != null && !recherche.trim().isEmpty())
                ? "%" + recherche.toLowerCase().trim() + "%"
                : null;

        boolean isAdmin = isAdmin();
        if (includeNonPublie && !isAdmin) {
            throw new IllegalArgumentException("L'option includeNonPublie est reservee aux administrateurs.");
        }

        Page<TexteJuridique> page = isAdmin
                ? texteRepository.searchAdvancedAll(searchPattern, legalDomain, typeTexte, pageable)
                : texteRepository.searchAdvancedPublished(searchPattern, legalDomain, typeTexte, pageable);

        return page
                .map(this::mapToDto);
    }

    // --- RECHERCHE AVANCÉE ELASTICSEARCH ---
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public List<com.lexdata.juridique.dto.SearchResultDto> advancedSearch(
            @RequestParam("q") @Size(min = 2, max = 200, message = "Le parametre q doit contenir entre 2 et 200 caracteres") String query,
            @RequestParam(name = "includeNonPublie", defaultValue = "false") boolean includeNonPublie) {
        boolean isAdmin = isAdmin();
        if (includeNonPublie && !isAdmin) {
            throw new IllegalArgumentException("L'option includeNonPublie est reservee aux administrateurs.");
        }
        return searchService.search(query.trim(), isAdmin && includeNonPublie);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    //@Cacheable(value = "textes", key = "#id")
    public ResponseEntity<TexteJuridiqueDto> getTexteById(@PathVariable("id") Long id) {
        boolean isAdmin = isAdmin();
        return (isAdmin ? texteRepository.findById(id) : texteRepository.findByIdAndEstPublieTrue(id))
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- ÉCRITURE (AGENTS SEULEMENT) ---
    @PostMapping
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> createTexte(@Valid @RequestBody TexteRequest request) {
        if (texteRepository.existsByReferenceOfficielle(request.getReferenceOfficielle())) {
            return ResponseEntity.badRequest().body("Erreur: Cette référence officielle existe déjà !");
        }

        TexteJuridique texte = mapToEntity(request);
        TexteJuridique savedTexte = texteRepository.save(texte);

        // Synchronisation Elasticsearch
        searchService.indexTexte(savedTexte);

        return ResponseEntity.ok(mapToDto(savedTexte));
    }

    // --- WORKFLOW ET VALIDATION ---
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long id,
            @RequestParam("status") TexteJuridique.WorkflowStatus status) {
        return texteRepository.findById(id)
                .map(texte -> {
                    texte.setStatut(status);
                    if (status == TexteJuridique.WorkflowStatus.PUBLIE) {
                        texte.setEstPublie(true);
                        // Notification asynchrone via RabbitMQ
                        eventPublisher.publishTextePublie(texte);
                    }
                    TexteJuridique updated = texteRepository.save(texte);
                    // Synchronisation Elasticsearch
                    searchService.indexTexte(updated);
                    return ResponseEntity.ok(mapToDto(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- ÉDITION (UPDATE) ---
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    @CacheEvict(value = "textes", key = "#id")
    public ResponseEntity<?> updateTexte(@PathVariable("id") Long id,
            @Valid @RequestBody TexteRequest request) {
        return texteRepository.findById(id)
                .map(texte -> {
                    texte.setTitre(request.getTitre());
                    texte.setReferenceOfficielle(request.getReferenceOfficielle());
                    texte.setType(TypeTexte.valueOf(request.getType()));
                    texte.setDomaine(LegalDomain.valueOf(request.getDomaine()));
                    texte.setDateSignature(request.getDateSignature());
                    texte.setDatePublicationJO(request.getDatePublicationJO());
                    texte.setDateEntreeEnVigueur(request.getDateEntreeEnVigueur());
                    texte.setJournalOfficielRef(request.getJournalOfficielRef());
                    texte.setSourceOfficielle(request.getSourceOfficielle());
                    texte.setContenu(request.getContenu());
                    texte.setEstPremium(request.isEstPremium());
                    TexteJuridique updated = texteRepository.save(texte);
                    // Synchronisation Elasticsearch
                    searchService.indexTexte(updated);
                    // Notification asynchrone pour les fiches synthèses
                    eventPublisher.publishTexteModifie(updated, "Modification du contenu ou des métadonnées");
                    return ResponseEntity.ok(mapToDto(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- SUPPRESSION ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    @CacheEvict(value = "textes", key = "#id")
    public ResponseEntity<?> deleteTexte(@PathVariable("id") Long id) {
        return texteRepository.findById(id)
                .map(texte -> {
                    texte.setDeleted(true);
                    texteRepository.save(texte);
                    // Synchronisation Elasticsearch (on supprime l'index pour la recherche)
                    searchService.removeTexte(id);
                    // Notification asynchrone pour les fiches synthèses
                    eventPublisher.publishTexteModifie(texte, "Texte supprimé (Soft Delete)");
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- VERSIONING (PREMIUM) ---
    @GetMapping("/{id}/versions")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('AGENT_ADMIN')")
    public ResponseEntity<List<TextVersionDto>> getTextVersions(@PathVariable("id") Long id) {
        return texteRepository.findById(id)
                .map(texte -> ResponseEntity.ok(
                        versionRepository.findByTexteOrderByDateVersionDesc(texte).stream()
                                .map(this::mapVersionToDto)
                                .toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/versions")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TextVersionDto> createVersion(
            @PathVariable("id") Long id,
            @RequestBody @Size(max = 400, message = "Le resume de modification est trop long") String summary) {
        return texteRepository.findById(id)
                .map(texte -> {
                    TextVersion version = TextVersion.builder()
                            .texte(texte)
                            .versionLabel("Version du " + LocalDate.now())
                            .contenu(texte.getContenu())
                            .modificationSummary(summary)
                            .build();
                    return ResponseEntity.ok(mapVersionToDto(versionRepository.save(version)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- ANNOTATIONS (PREMIUM) ---
    // 1. Les annotations d'un texte spécifique
    @GetMapping("/{id}/annotations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LegalAnnotationDto>> getMyAnnotationsForText(@PathVariable("id") Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return texteRepository.findById(id)
                .map(texte -> ResponseEntity.ok(
                        annotationRepository.findByUserIdAndTexte(userId, texte).stream()
                                .map(this::mapAnnotationToDto)
                                .toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    // 2. Créer une annotation sur un texte
    @PostMapping("/{id}/annotations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LegalAnnotationDto> createAnnotation(
            @PathVariable("id") Long id,
            @Valid @RequestBody AnnotationRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return texteRepository.findById(id)
                .map(texte -> {
                    LegalAnnotation annotation = LegalAnnotation.builder()
                            .userId(userId)
                            .texte(texte)
                            .note(request.getNote().trim())
                            .build();
                    return ResponseEntity.ok(mapAnnotationToDto(annotationRepository.save(annotation)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. NOUVEAU : Récupérer TOUTES les annotations de l'utilisateur (Pour le
    // Dashboard)
    @GetMapping("/annotations/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LegalAnnotationDto>> getAllMyAnnotations() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<LegalAnnotationDto> myAnnotations = annotationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapAnnotationToDto)
                .toList();
        return ResponseEntity.ok(myAnnotations);
    }

    // Helper mapping DTO -> Entity
    private TexteJuridique mapToEntity(TexteRequest request) {
        return TexteJuridique.builder()
                .titre(request.getTitre())
                .referenceOfficielle(request.getReferenceOfficielle())
                .type(TypeTexte.valueOf(request.getType()))
                .domaine(LegalDomain.valueOf(request.getDomaine()))
                .dateSignature(request.getDateSignature())
                .datePublicationJO(request.getDatePublicationJO())
                .dateEntreeEnVigueur(request.getDateEntreeEnVigueur())
                .journalOfficielRef(request.getJournalOfficielRef())
                .sourceOfficielle(request.getSourceOfficielle())
                .contenu(request.getContenu())
                .estPremium(request.isEstPremium())
                .build();
    }

    // Mapping Entity -> DTO
    private TexteJuridiqueDto mapToDto(TexteJuridique texte) {
        return TexteJuridiqueDto.builder()
                .id(texte.getId())
                .titre(texte.getTitre())
                .referenceOfficielle(texte.getReferenceOfficielle())
                .type(texte.getType().name())
                .domaine(texte.getDomaine().name())
                .statut(texte.getStatut().name())
                .dateSignature(texte.getDateSignature())
                .datePublicationJO(texte.getDatePublicationJO())
                .dateEntreeEnVigueur(texte.getDateEntreeEnVigueur())
                .journalOfficielRef(texte.getJournalOfficielRef())
                .sourceOfficielle(texte.getSourceOfficielle())
                .contenu(texte.getContenu())
                .estPublie(texte.getEstPublie())
                .estPremium(texte.getEstPremium())
                .build();
    }

    private LegalAnnotationDto mapAnnotationToDto(LegalAnnotation annotation) {
        return LegalAnnotationDto.builder()
                .id(annotation.getId())
                .legalTextId(annotation.getTexte().getId())
                .note(annotation.getNote())
                .createdAt(annotation.getCreatedAt())
                .updatedAt(annotation.getUpdatedAt())
                .build();
    }

    private TextVersionDto mapVersionToDto(TextVersion version) {
        return TextVersionDto.builder()
                .id(version.getId())
                .legalTextId(version.getTexte().getId())
                .versionLabel(version.getVersionLabel())
                .contenu(version.getContenu())
                .dateVersion(version.getDateVersion())
                .modificationSummary(version.getModificationSummary())
                .build();
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_AGENT_ADMIN".equals(a.getAuthority())
                        || "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
    }
}
