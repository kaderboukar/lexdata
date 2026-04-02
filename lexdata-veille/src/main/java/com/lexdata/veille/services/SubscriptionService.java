package com.lexdata.veille.services;

import com.lexdata.veille.dto.VeilleSubscriptionDto;
import com.lexdata.veille.models.VeilleSubscription;
import com.lexdata.veille.payload.VeilleSubscriptionRequest;
import com.lexdata.veille.repository.VeilleSubscriptionRepository;
import com.lexdata.veille.util.VeilleDomainNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final VeilleSubscriptionRepository subscriptionRepository;

    public VeilleSubscriptionDto create(VeilleSubscriptionRequest request) {
        VeilleSubscription saved = subscriptionRepository.save(VeilleSubscription.builder()
                .userId(currentUserId())
                .domaines(normalizeDomainSet(request.getDomaines()))
                .textTypes(normalizeTextTypeSet(request.getTextTypes()))
                .active(Boolean.TRUE.equals(request.getActive()))
                .build());
        return toDto(saved);
    }

    public VeilleSubscriptionDto update(Long id, VeilleSubscriptionRequest request) {
        VeilleSubscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Veille introuvable"));
        if (!sub.getUserId().equals(currentUserId())) {
            throw new IllegalArgumentException("Acces interdit");
        }
        sub.setDomaines(normalizeDomainSet(request.getDomaines()));
        sub.setTextTypes(normalizeTextTypeSet(request.getTextTypes()));
        sub.setActive(Boolean.TRUE.equals(request.getActive()));
        return toDto(subscriptionRepository.save(sub));
    }

    public VeilleSubscriptionDto toggle(Long id, boolean active) {
        VeilleSubscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Veille introuvable"));
        if (!sub.getUserId().equals(currentUserId())) {
            throw new IllegalArgumentException("Acces interdit");
        }
        sub.setActive(active);
        return toDto(subscriptionRepository.save(sub));
    }

    public List<VeilleSubscriptionDto> mySubscriptions() {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(currentUserId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<VeilleSubscription> activeSubscriptions() {
        return subscriptionRepository.findByActiveTrue();
    }

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Set<String> normalizeDomainSet(java.util.Set<String> values) {
        if (values == null) {
            return Collections.emptySet();
        }
        return values.stream()
                .map(v -> v == null ? "" : v.trim().toUpperCase())
                .filter(v -> !v.isBlank())
                .map(VeilleDomainNormalizer::canonical)
                .collect(Collectors.toSet());
    }

    private Set<String> normalizeTextTypeSet(java.util.Set<String> values) {
        if (values == null) {
            return Collections.emptySet();
        }
        return values.stream()
                .map(v -> v == null ? "" : v.trim().toUpperCase())
                .filter(v -> !v.isBlank())
                .collect(Collectors.toSet());
    }

    private VeilleSubscriptionDto toDto(VeilleSubscription sub) {
        return VeilleSubscriptionDto.builder()
                .id(sub.getId())
                .domaines(sub.getDomaines())
                .textTypes(sub.getTextTypes())
                .active(sub.isActive())
                .createdAt(sub.getCreatedAt())
                .updatedAt(sub.getUpdatedAt())
                .build();
    }
}
