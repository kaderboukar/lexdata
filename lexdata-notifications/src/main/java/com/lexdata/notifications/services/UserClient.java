package com.lexdata.notifications.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "lexdata-user-service", path = "/api/preferences", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/by-domain/{domain}")
    List<String> getUsernamesByDomain(@PathVariable("domain") String domain);
}
