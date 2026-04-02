package com.lexdata.notifications.services;

import com.lexdata.notifications.config.FeignInternalAuthConfiguration;
import com.lexdata.notifications.dto.internal.AuthIdUsername;
import com.lexdata.notifications.dto.internal.AuthUserIdPage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "lexdata-auth-service",
        path = "/api/auth/internal",
        configuration = FeignInternalAuthConfiguration.class,
        fallback = AuthInternalClientFallback.class)
public interface AuthInternalClient {

    @PostMapping("/user-ids/by-usernames")
    List<Long> findUserIdsByUsernames(@RequestBody List<String> usernames);

    @PostMapping("/users/resolve")
    List<AuthIdUsername> resolveUsers(@RequestBody List<Long> userIds);

    @GetMapping("/user-ids")
    AuthUserIdPage listUserIds(
            @RequestParam(required = false) String role,
            @RequestParam("page") int page,
            @RequestParam("size") int size);
}
