package com.lexdata.notifications.services;

import com.lexdata.notifications.models.Notification;
import com.lexdata.notifications.models.NotificationPreference;
import com.lexdata.notifications.repository.NotificationPreferenceRepository;
import com.lexdata.notifications.repository.NotificationRepository;
import com.lexdata.notifications.services.strategies.EmailNotificationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigestService {

    private static final int MAX_ITEMS_PER_DIGEST = 50;

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailNotificationStrategy emailStrategy;
    private final UserDirectoryService userDirectoryService;

    @Value("${lexdata.notifications.digest.max-items:" + MAX_ITEMS_PER_DIGEST + "}")
    private int maxItemsPerDigest;

    @Scheduled(cron = "${lexdata.notifications.digest.cron:0 0 8 * * *}")
    @Transactional
    public void sendDailyDigests() {
        log.debug("event=digest_job_started");

        List<Notification> pendingNotifications = notificationRepository.findByChannelAndAggregatedFalseAndStatus(
                Notification.NotificationChannel.EMAIL,
                Notification.NotificationStatus.SENT);

        if (pendingNotifications.isEmpty()) {
            log.debug("event=digest_job_empty");
            return;
        }

        Map<Long, List<Notification>> byUser = pendingNotifications.stream()
                .collect(Collectors.groupingBy(Notification::getUserId));

        byUser.forEach((userId, notifications) -> {
            NotificationPreference prefs = preferenceRepository.findByUserId(userId).orElse(null);
            if (prefs == null || !prefs.isEmailEnabled()
                    || prefs.getDigestFrequency() != NotificationPreference.DigestFrequency.DAILY) {
                return;
            }

            String username = userDirectoryService.resolveUsername(userId, null);
            List<Notification> sorted = notifications.stream()
                    .sorted(Comparator.comparing(Notification::getDateCreation))
                    .toList();
            int cap = Math.max(1, maxItemsPerDigest);
            List<Notification> forEmail = sorted.size() > cap ? sorted.subList(sorted.size() - cap, sorted.size()) : sorted;

            sendAggregatedEmail(userId, username, forEmail);

            forEmail.forEach(n -> n.setAggregated(true));
            notificationRepository.saveAll(forEmail);
        });

        log.debug("event=digest_job_finished users={}", byUser.size());
    }

    private void sendAggregatedEmail(Long userId, String username, List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return;
        }
        String titre = "Votre résumé quotidien LexData — " + notifications.size() + " élément(s)";

        StringBuilder messageBody = new StringBuilder();
        messageBody.append("Bonjour,\n\n");
        messageBody.append("Récapitulatif par type :\n\n");

        Map<Notification.NotificationType, List<Notification>> byType = notifications.stream()
                .collect(Collectors.groupingBy(Notification::getType));

        for (Notification.NotificationType t : Notification.NotificationType.values()) {
            List<Notification> slice = byType.get(t);
            if (slice == null || slice.isEmpty()) {
                continue;
            }
            messageBody.append("—— ").append(t.name()).append(" ——\n");
            for (Notification n : slice) {
                messageBody.append("• ").append(n.getTitre()).append("\n");
                messageBody.append(n.getMessage()).append("\n");
                if (n.getLink() != null && !n.getLink().isBlank()) {
                    messageBody.append("  → ").append(n.getLink()).append("\n");
                }
                messageBody.append("\n");
            }
        }

        if (notifications.size() >= maxItemsPerDigest) {
            messageBody.append("\n(Limité aux ").append(maxItemsPerDigest).append(" entrées les plus récentes.)\n");
        }

        messageBody.append("\n—\nL'équipe LexData");

        emailStrategy.sendDigestSummary(userId, username, titre, messageBody.toString());
    }
}
