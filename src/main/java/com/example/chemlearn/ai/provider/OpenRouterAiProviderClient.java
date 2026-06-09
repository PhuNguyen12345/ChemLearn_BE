package com.example.chemlearn.ai.provider;

import com.example.chemlearn.ai.config.AiProperties;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "openrouter")
public class OpenRouterAiProviderClient implements AiProviderClient {
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String chat(String prompt) {
        return requestCompletion(prompt, 0.35);
    }

    @Override
    public String chatWithImage(String prompt, String mimeType, byte[] imageBytes) {
        throw new CustomExceptions.BadRequestException("OpenRouter image chat is not configured. Use ai.provider=gemini for photo support.");
    }

    @Override
    public String generateExam(String prompt) {
        return requestCompletion(prompt, 0.2);
    }

    private String requestCompletion(String prompt, double fallbackTemperature) {
        validateConfig();

        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", aiProperties.getModel(),
                    "temperature", aiProperties.getTemperature() == null ? fallbackTemperature : aiProperties.getTemperature(),
                    "messages", List.of(Map.of(
                            "role", "user",
                            "content", prompt
                    ))
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiProperties.getOpenrouter().getBaseUrl()))
                    .timeout(Duration.ofSeconds(aiProperties.getRequestTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiProperties.getApiKey())
                    .header("HTTP-Referer", aiProperties.getAppUrl())
                    .header("X-Title", aiProperties.getAppTitle())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(aiProperties.getRequestTimeoutSeconds()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new CustomExceptions.BadRequestException("OpenRouter API error: " + response.statusCode() + " - " + extractError(response.body()));
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new CustomExceptions.BadRequestException("OpenRouter returned an empty response");
            }
            return content.asText();
        } catch (IOException ex) {
            throw new CustomExceptions.BadRequestException("Cannot call OpenRouter API: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CustomExceptions.BadRequestException("OpenRouter API request was interrupted");
        }
    }

    private void validateConfig() {
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            throw new CustomExceptions.BadRequestException("Missing AI_API_KEY for OpenRouter provider");
        }
        if (aiProperties.getModel() == null || aiProperties.getModel().isBlank() || "mock".equalsIgnoreCase(aiProperties.getModel())) {
            throw new CustomExceptions.BadRequestException("Missing ai.model for OpenRouter provider");
        }
    }

    private String extractError(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "empty error response";
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = root.path("error").path("message").asText();
            if (!message.isBlank()) {
                return message;
            }
        } catch (IOException ignored) {
            return responseBody;
        }
        return responseBody;
    }
}
