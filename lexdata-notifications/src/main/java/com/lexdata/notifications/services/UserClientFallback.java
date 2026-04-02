package com.lexdata.notifications.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserClientFallback implements UserClient {
    private static final Logger logger = LoggerFactory.getLogger(UserClientFallback.class);

    @Override
    public List<String> getUsernamesByDomain(String domain) {
        logger.error("Failed to fetch users for domain: {}. User Service is unavailable. Returning empty list.",
                domain);
        return Collections.emptyList();
    }
}
