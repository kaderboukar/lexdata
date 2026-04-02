package com.lexdata.auth.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserProfileClientFallback implements UserProfileClient {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileClientFallback.class);

    @Override
    public void createProfile(String token, Map<String, Object> profileRequest) {
        logger.error("Failed to create profile for user. User Service is unavailable. Fallback executed.");
        // Non-blocking fallback: Log and maybe queue for later processing if critical
    }
}
