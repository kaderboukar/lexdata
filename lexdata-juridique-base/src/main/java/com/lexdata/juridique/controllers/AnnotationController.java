package com.lexdata.juridique.controllers;

import com.lexdata.juridique.dto.LegalAnnotationDto;
import com.lexdata.juridique.payload.AnnotationRequest;
import com.lexdata.juridique.repository.LegalAnnotationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/juridique/annotations")
@RequiredArgsConstructor
public class AnnotationController {

    private final LegalAnnotationRepository annotationRepository;

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LegalAnnotationDto> updateAnnotation(
            @PathVariable Long id,
            @Valid @RequestBody AnnotationRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return annotationRepository.findByIdAndUserId(id, userId)
                .map(annotation -> {
                    annotation.setNote(request.getNote().trim());
                    return ResponseEntity.ok(toDto(annotationRepository.save(annotation)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAnnotation(@PathVariable Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return annotationRepository.findByIdAndUserId(id, userId)
                .map(annotation -> {
                    annotationRepository.delete(annotation);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private LegalAnnotationDto toDto(com.lexdata.juridique.models.LegalAnnotation annotation) {
        return LegalAnnotationDto.builder()
                .id(annotation.getId())
                .legalTextId(annotation.getTexte().getId())
                .note(annotation.getNote())
                .createdAt(annotation.getCreatedAt())
                .updatedAt(annotation.getUpdatedAt())
                .build();
    }
}
