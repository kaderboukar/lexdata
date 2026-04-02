package com.lexdata.notifications.services;

import com.lexdata.notifications.dto.internal.AuthIdUsername;
import com.lexdata.notifications.dto.internal.AuthUserIdPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class AuthInternalClientFallback implements AuthInternalClient {

    @Override
    public List<Long> findUserIdsByUsernames(List<String> usernames) {
        log.warn("event=auth_internal_fallback op=findUserIdsByUsernames");
        return Collections.emptyList();
    }

    @Override
    public List<AuthIdUsername> resolveUsers(List<Long> userIds) {
        log.warn("event=auth_internal_fallback op=resolveUsers");
        return Collections.emptyList();
    }

    @Override
    public AuthUserIdPage listUserIds(String role, int page, int size) {
        log.warn("event=auth_internal_fallback op=listUserIds");
        return new AuthUserIdPage(Collections.emptyList(), 0, 0, 0);
    }
}
