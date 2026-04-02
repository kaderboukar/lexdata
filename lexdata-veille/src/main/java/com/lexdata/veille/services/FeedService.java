package com.lexdata.veille.services;

import com.lexdata.veille.models.AlerteVeille;
import com.lexdata.veille.repository.AlerteVeilleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final StringRedisTemplate redisTemplate;
    private final UserClient userClient;
    private final AlerteVeilleRepository alerteRepository;

    private static final String FEED_KEY_PREFIX = "feed:user:";

    /**
     * Fan-out : Pousse l'ID de l'alerte dans les feeds Redis de tous les
     * utilisateurs abonnés.
     */
    public void fanOut(AlerteVeille alerte) {
        if (alerte.getStatus() != AlerteVeille.AlertStatus.PUBLISHED) {
            return;
        }
        Set<String> domaines = alerte.getDomainesCibles();
        if (domaines == null || domaines.isEmpty()) {
            log.warn("L'alerte {} n'a pas de domaines cibles. Fan-out annulé.", alerte.getId());
            return;
        }

        // Pour chaque domaine, on récupère les utilisateurs abonnés
        Set<String> allUsernames = domaines.stream()
                .flatMap(domaine -> userClient.getUsernamesByDomain(domaine).stream())
                .collect(Collectors.toSet());

        log.info("Début du fan-out pour l'alerte {} vers {} utilisateurs.", alerte.getId(), allUsernames.size());

        for (String username : allUsernames) {
            String key = FEED_KEY_PREFIX + username;
            // On ajoute l'ID en tête de liste (LPUSH)
            redisTemplate.opsForList().leftPush(key, String.valueOf(alerte.getId()));
            // On limite la taille du feed (optionnel, ex: 100 derniers)
            redisTemplate.opsForList().trim(key, 0, 99);
        }

        log.info("Fan-out terminé pour l'alerte {}.", alerte.getId());
    }

    /**
     * Récupère le feed pré-calculé depuis Redis.
     */
    public List<AlerteVeille> getFeed(String username, int page, int size) {
        String key = FEED_KEY_PREFIX + username;
        int start = page * size;
        int end = start + size - 1;

        List<String> idsStr = redisTemplate.opsForList().range(key, start, end);
        if (idsStr == null || idsStr.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = idsStr.stream().map(Long::valueOf).collect(Collectors.toList());

        // On récupère les alertes depuis la DB en gardant l'ordre
        // Note: findAllById ne garantit pas l'ordre, on pourrait avoir besoin d'un tri
        // manuel ou d'une requête spécifique
        List<AlerteVeille> alertes = alerteRepository.findAllById(ids);

        // Tri manuel pour respecter l'ordre de Redis (chronologique inverse car LPUSH)
        alertes.sort((a, b) -> ids.indexOf(a.getId()) - ids.indexOf(b.getId()));

        return alertes;
    }
}
