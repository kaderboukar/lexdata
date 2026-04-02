package com.lexdata.notifications.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

public class FeignInternalAuthConfiguration {

    @Bean
    public RequestInterceptor lexdataInternalApiKeyInterceptor(@Value("${lexdata.internal.apiKey:}") String apiKey) {
        return template -> {
            if (StringUtils.hasText(apiKey)) {
                template.header("X-Lexdata-Internal-Key", apiKey);
            }
        };
    }
}
