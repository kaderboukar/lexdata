package com.lexdata.notifications.services;

import com.lexdata.notifications.dto.AdminBroadcastRequest;
import com.lexdata.notifications.dto.internal.AuthUserIdPage;
import com.lexdata.notifications.models.Notification;
import com.lexdata.notifications.models.NotificationDelivery;
import com.lexdata.notifications.models.NotificationPreference;
import com.lexdata.notifications.repository.NotificationDeliveryRepository;
import com.lexdata.notifications.repository.NotificationPreferenceRepository;
import com.lexdata.notifications.repository.NotificationRepository;
import com.lexdata.notifications.services.dispatch.OutboundNotificationContext;
import com.lexdata.notifications.services.strategies.NotificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

    private static final int BROADCAST_PAGE_SIZE = 500;

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final Map<Notification.NotificationChannel, NotificationStrategy> strategyMap;
    private final UserDirectoryService userDirectoryService;
    private final AuthInternalClient authInternalClient;

    public NotificationService(
            NotificationPreferenceRepository preferenceRepository,
            NotificationRepository notificationRepository,
            NotificationDeliveryRepository deliveryRepository,
            List<NotificationStrategy> strategies,
            UserDirectoryService userDirectoryService,
            AuthInternalClient authInternalClient) {
        this.preferenceRepository = preferenceRepository;
        this.notificationRepository = notificationRepository;
        this.deliveryRepository = deliveryRepository;
        this.strategyMap = strategies.stream().collect(Collectors.toMap(NotificationStrategy::getChannel, s -> s));
        this.userDirectoryService = userDirectoryService;
        this.authInternalClient = authInternalClient;
    }

    @Async
    public void sendNotification(Long userId, String usernameHint, String titre, String message,
            Notification.NotificationType type, String link) {
        sendNotification(userId, usernameHint, titre, message, type, link, null);
    }

    /**
     * @param channelOverride si non null / non vide : remplace les canaux déduits des préférences
     *                        (ex. campagne admin). Pour l'email, un override contenant EMAIL contourne le digest quotidien.
     */
    @Async
    public void sendNotification(Long userId, String usernameHint, String titre, String message,
            Notification.NotificationType type, String link,
            Set<Notification.NotificationChannel> channelOverride) {

        if (userId == null) {
            log.warn("event=notification_skipped reason=null_userId");
            return;
        }

        NotificationPreference prefs = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        if (!isTypeEnabled(prefs, type)) {
            log.debug("event=notification_skipped reason=type_disabled userId={} type={}", userId, type);
            return;
        }

        Set<Notification.NotificationChannel> channels = resolveChannels(prefs, channelOverride);
        String username = userDirectoryService.resolveUsername(userId, usernameHint);

        for (Notification.NotificationChannel channel : channels) {
            if (channel == Notification.NotificationChannel.EMAIL
                    && prefs.getDigestFrequency() == NotificationPreference.DigestFrequency.DAILY
                    && (channelOverride == null || !channelOverride.contains(Notification.NotificationChannel.EMAIL))) {
                persistDigestEmailBuffer(userId, titre, message, type, link);
                continue;
            }
            dispatchChannel(userId, username, titre, message, type, link, channel);
        }
    }

    @Async
    public void broadcast(AdminBroadcastRequest request) {
        validateBroadcast(request);
        Set<Notification.NotificationChannel> override = request.getChannels() == null || request.getChannels().isEmpty()
                ? null
                : new LinkedHashSet<>(request.getChannels());
        Notification.NotificationType type =
                request.getType() != null ? request.getType() : Notification.NotificationType.SYSTEME;

        List<Long> targets = listBroadcastTargets(request);
        log.debug("event=admin_broadcast targetCount={} targetType={}", targets.size(), request.getTargetType());

        for (Long userId : targets) {
            if (userId == null) {
                continue;
            }
            sendNotification(userId, null, request.getTitle(), request.getMessage(), type, request.getLink(), override);
        }
    }

    private void validateBroadcast(AdminBroadcastRequest request) {
        switch (request.getTargetType()) {
            case ROLE -> {
                if (request.getRole() == null || request.getRole().isBlank()) {
                    throw new IllegalArgumentException("role est requis pour targetType=ROLE");
                }
            }
            case USERS -> {
                if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
                    throw new IllegalArgumentException("userIds est requis pour targetType=USERS");
                }
            }
            case ALL -> {
                /* ok */
            }
        }
    }

    private List<Long> listBroadcastTargets(AdminBroadcastRequest request) {
        return switch (request.getTargetType()) {
            case USERS -> request.getUserIds().stream().filter(Objects::nonNull).distinct().toList();
            case ROLE -> {
                String role = request.getRole().trim();
                List<Long> acc = new ArrayList<>();
                for (int page = 0; ; page++) {
                    AuthUserIdPage p = authInternalClient.listUserIds(role, page, BROADCAST_PAGE_SIZE);
                    if (p.content() == null || p.content().isEmpty()) {
                        break;
                    }
                    acc.addAll(p.content());
                    if (p.totalPages() <= 0 || page + 1 >= p.totalPages()) {
                        break;
                    }
                }
                yield acc;
            }
            case ALL -> {
                List<Long> acc = new ArrayList<>();
                for (int page = 0; ; page++) {
                    AuthUserIdPage p = authInternalClient.listUserIds(null, page, BROADCAST_PAGE_SIZE);
                    if (p.content() == null || p.content().isEmpty()) {
                        break;
                    }
                    acc.addAll(p.content());
                    if (p.totalPages() <= 0 || page + 1 >= p.totalPages()) {
                        break;
                    }
                }
                yield acc;
            }
        };
    }

    private void dispatchChannel(Long userId, String username, String titre, String message,
            Notification.NotificationType type, String link,
            Notification.NotificationChannel channel) {

        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setTitre(titre);
        notif.setMessage(message);
        notif.setLink(link);
        notif.setType(type);
        notif.setChannel(channel);
        notif.setStatus(Notification.NotificationStatus.PENDING);
        notif.setAggregated(false);
        notif = notificationRepository.save(notif);

        NotificationDelivery delivery = new NotificationDelivery();
        delivery.setNotificationId(notif.getId());
        delivery.setUserId(userId);
        delivery.setChannel(channel);
        delivery.setStatus(NotificationDelivery.DeliveryStatus.PENDING);
        delivery.setDigestHold(false);
        delivery = deliveryRepository.save(delivery);

        NotificationStrategy strategy = strategyMap.get(channel);
        if (strategy == null) {
            log.warn("event=notification_no_strategy channel={}", channel);
            return;
        }

        strategy.dispatch(new OutboundNotificationContext(userId, username, notif, delivery));
    }

    private void persistDigestEmailBuffer(Long userId, String titre, String message,
            Notification.NotificationType type, String link, Notification.NotificationChannel channel) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setTitre(titre);
        notif.setMessage(message);
        notif.setLink(link);
        notif.setType(type);
        notif.setChannel(channel);
        notif.setStatus(Notification.NotificationStatus.SENT);
        notif.setAggregated(false);
        notificationRepository.save(notif);
    }

    private void persistDigestEmailBuffer(Long userId, String titre, String message,
            Notification.NotificationType type, String link) {
        persistDigestEmailBuffer(userId, titre, message, type, link, Notification.NotificationChannel.EMAIL);
    }

    private Set<Notification.NotificationChannel> resolveChannels(NotificationPreference prefs,
            Set<Notification.NotificationChannel> override) {
        if (override != null && !override.isEmpty()) {
            return new LinkedHashSet<>(override);
        }
        Set<Notification.NotificationChannel> set = new LinkedHashSet<>();
        if (prefs.isInAppEnabled()) {
            set.add(Notification.NotificationChannel.IN_APP);
        }
        if (prefs.isEmailEnabled()) {
            set.add(Notification.NotificationChannel.EMAIL);
        }
        if (prefs.isPushEnabled()) {
            set.add(Notification.NotificationChannel.PUSH);
        }
        if (prefs.isSmsEnabled()) {
            set.add(Notification.NotificationChannel.SMS);
        }
        return set;
    }

    private boolean isTypeEnabled(NotificationPreference prefs, Notification.NotificationType type) {
        return switch (type) {
            case VEILLE -> prefs.isVeilleAlerts();
            case SYNTHESE -> prefs.isSyntheseAlerts();
            case CALENDRIER -> prefs.isCalendrierAlerts();
            case CONTRAT -> prefs.isContratAlerts();
            case CONSULTATION -> prefs.isConsultationAlerts();
            case PAIEMENT -> prefs.isPaiementAlerts();
            case TRIBUNE -> prefs.isTribuneAlerts();
            case SYSTEME -> true;
        };
    }

    private NotificationPreference createDefaultPreferences(Long userId) {
        NotificationPreference p = new NotificationPreference();
        p.setUserId(userId);
        return preferenceRepository.save(p);
    }
}
