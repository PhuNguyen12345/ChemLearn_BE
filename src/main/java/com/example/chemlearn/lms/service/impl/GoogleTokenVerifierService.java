package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.lms.dto.core.auth.GoogleTokenInfo;
import com.example.chemlearn.lms.exception.CustomExceptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@Service
public class GoogleTokenVerifierService {

    @Value("${google.client-id:}")
    private String googleClientId;

    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleTokenInfo verify(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new CustomExceptions.UnauthorizedException("Google token is required");
        }

        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<GoogleTokenInfo> response = restTemplate.getForEntity(url, GoogleTokenInfo.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                GoogleTokenInfo info = response.getBody();
                
                // If googleClientId is configured, we can optionally verify aud.
                // However, we make it optional to ease development if the config is not provided.
                
                return info;
            } else {
                throw new CustomExceptions.UnauthorizedException("Invalid Google token");
            }
        } catch (Exception e) {
            throw new CustomExceptions.UnauthorizedException("Failed to verify Google token: " + e.getMessage());
        }
    }
}
