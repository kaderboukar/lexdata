package com.lexdata.veille.services;

import com.lexdata.veille.dto.UserAlertDto;
import com.lexdata.veille.models.AlerteVeille;
import com.lexdata.veille.models.UserAlert;
import com.lexdata.veille.models.VeilleSubscription;
import com.lexdata.veille.repository.UserAlertRepository;
import com.lexdata.veille.util.VeilleDomainNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserAlertService {
    private final UserAlertRepository userAlertRepository;
    private final SubscriptionService subscriptionService;

    public void createPersonalizedAlerts(AlerteVeille alert) {
        Set<String> alertDomains = VeilleDomainNormalizer.canonicalDomainSet(alert.getDomainesCibles());
        boolean alertSansDomaine = alertDomains.isEmpty();
        String alertType = alert.getTexteType();
        boolean alertSansType = alertType == null || alertType.isBlank();
        String alertTypeNorm = alertSansType ? null : alertType.trim().toUpperCase();

        List<VeilleSubscription> subs = subscriptionService.activeSubscriptions();
        for (VeilleSubscription sub : subs) {
            Set<String> subDomains = VeilleDomainNormalizer.canonicalDomainSet(sub.getDomaines());
            boolean domainMatch = subDomains.isEmpty()
                    || alertSansDomaine
                    || alertDomains.stream().anyMatch(subDomains::contains);
            boolean typeMatch = sub.getTextTypes().isEmpty()
                    || alertSansType
                    || (alertTypeNorm != null && sub.getTextTypes().contains(alertTypeNorm));
            if (!domainMatch || !typeMatch) {
                continue;
            }
            if (!userAlertRepository.existsByUserIdAndAlertId(sub.getUserId(), alert.getId())) {
                userAlertRepository.save(UserAlert.builder().userId(sub.getUserId()).alert(alert).build());
            }
        }
    }

    public Page<UserAlertDto> myAlerts(Boolean unreadOnly, String domaine, String type, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        String userId = currentUserId();
        Boolean readFilter = unreadOnly == null ? null : !unreadOnly ? null : Boolean.FALSE;
        LocalDateTime from = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime to = toDate == null ? null : toDate.plusDays(1).atStartOfDay().minusNanos(1);
        String domaineCanon = domaine == null ? null : VeilleDomainNormalizer.canonical(domaine);
        return userAlertRepository.searchMyAlerts(
                userId,
                readFilter,
                domaineCanon,
                type == null ? null : type.toUpperCase(),
                from,
                to,
                pageable).map(this::toDto);
    }

    public UserAlertDto markAsRead(Long userAlertId, boolean isRead) {
        UserAlert ua = userAlertRepository.findByIdAndUserId(userAlertId, currentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Alerte utilisateur introuvable"));
        ua.setRead(isRead);
        ua.setReadAt(isRead ? LocalDateTime.now() : null);
        return toDto(userAlertRepository.save(ua));
    }

    public void softDelete(Long userAlertId) {
        UserAlert ua = userAlertRepository.findByIdAndUserId(userAlertId, currentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Alerte utilisateur introuvable"));
        ua.setDeletedAt(LocalDateTime.now());
        userAlertRepository.save(ua);
    }

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private UserAlertDto toDto(UserAlert ua) {
        AlerteVeille a = ua.getAlert();
        return UserAlertDto.builder()
                .id(ua.getId())
                .alertId(a.getId())
                .userId(ua.getUserId())
                .isRead(ua.isRead())
                .readAt(ua.getReadAt())
                .createdAt(ua.getCreatedAt())
                .title(a.getTitre())
                .summary(a.getResumeClarifie())
                .eventType(a.getEventType().name())
                .status(a.getStatus().name())
                .legalTextId(a.getTexteJuridiqueId())
                .textType(a.getTexteType())
                .domaines(a.getDomainesCibles())
                .legalTextUrl(a.getLienTexte())
                .syntheseUrl(a.getLienSynthese())
                .alertDate(a.getDateCreation())
                .build();
    }
}
