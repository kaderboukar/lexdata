package com.lexdata.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "lexdata-user-service", url = "${app.user-service.url:http://localhost:8082}", fallback = UserProfileClientFallback.class)
public interface UserProfileClient {

    @PostMapping("/api/profiles/me")
    void createProfile(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> profileRequest);
}
