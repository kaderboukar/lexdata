package com.lexdata.synthese.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class LegalTextValidationService {

    private final RestTemplate restTemplate;

    @Value("${lexdata.juridique-base-url:http://localhost:9101}")
    private String juridiqueBaseUrl;

    public boolean exists(Long legalTextId) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    juridiqueBaseUrl + "/api/juridique/textes/" + legalTextId,
                    String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException ex) {
            return false;
        }
    }
}
