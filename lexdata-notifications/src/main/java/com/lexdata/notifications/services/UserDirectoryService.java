package com.lexdata.notifications.services;

import com.lexdata.notifications.dto.internal.AuthIdUsername;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDirectoryService {

    private final AuthInternalClient authInternalClient;

    public String resolveUsername(Long userId, String hintUsername) {
        if (hintUsername != null && !hintUsername.isBlank()) {
            return hintUsername.trim();
        }
        List<AuthIdUsername> rows = authInternalClient.resolveUsers(List.of(userId));
        if (rows == null || rows.isEmpty()) {
            log.warn("event=user_resolve_failed userId={}", userId);
            return "user-" + userId;
        }
        return rows.get(0).username();
    }

    public Map<Long, String> resolveUsernames(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = new ArrayList<>(new LinkedHashSet<>(userIds));
        List<AuthIdUsername> rows = authInternalClient.resolveUsers(ids);
        Map<Long, String> map = new HashMap<>();
        if (rows != null) {
            for (AuthIdUsername row : rows) {
                map.put(row.userId(), row.username());
            }
        }
        for (Long id : ids) {
            map.putIfAbsent(id, "user-" + id);
        }
        return map;
    }

    /**
     * Résolution des identifiants auth à partir des noms d'utilisateur (ex. veille par domaine).
     */
    public List<Long> authUserIdsForUsernames(Collection<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return List.of();
        }
        return authInternalClient.findUserIdsByUsernames(new ArrayList<>(usernames));
    }
}
