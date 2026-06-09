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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
public class GeminiAiProviderClient implements AiProviderClient {
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String chat(String prompt) {
        return requestGenerateContent(prompt, false, 0.35);
    }

    @Override
    public String chatWithImage(String prompt, String mimeType, byte[] imageBytes) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        List<Map<String, Object>> parts = List.of(
                Map.of("text", prompt),
                Map.of("inlineData", Map.of(
                        "mimeType", mimeType,
                        "data", base64Image
                ))
        );
        return requestGenerateContent(parts, false, 0.25);
    }

    @Override
    public String generateExam(String prompt) {
        return requestGenerateContent(prompt, true, 0.2);
    }

    private String requestGenerateContent(String prompt, boolean jsonMode, double fallbackTemperature) {
        return requestGenerateContent(List.of(Map.of("text", prompt)), jsonMode, fallbackTemperature);
    }

    private String requestGenerateContent(List<Map<String, Object>> parts, boolean jsonMode, double fallbackTemperature) {
        validateConfig();

        try {
            Map<String, Object> generationConfig = new LinkedHashMap<>();
            generationConfig.put("temperature", aiProperties.getTemperature() == null ? fallbackTemperature : aiProperties.getTemperature());
            generationConfig.put("maxOutputTokens", 8192);
            if (aiProperties.getModel() != null && aiProperties.getModel().contains("2.5")) {
                generationConfig.put("thinkingConfig", Map.of("thinkingBudget", 0));
            }
            if (jsonMode) {
                generationConfig.put("responseMimeType", "application/json");
                generationConfig.put("responseSchema", examResponseSchema());
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("contents", List.of(Map.of(
                    "role", "user",
                    "parts", parts
            )));
            body.put("generationConfig", generationConfig);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(buildEndpoint()))
                    .timeout(Duration.ofSeconds(aiProperties.getRequestTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", aiProperties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(aiProperties.getRequestTimeoutSeconds()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new CustomExceptions.BadRequestException("Gemini API error: " + response.statusCode() + " - " + extractError(response.body()));
            }

            String text = extractText(response.body());
            if (text.isBlank()) {
                throw new CustomExceptions.BadRequestException("Gemini returned an empty response");
            }
            return text;
        } catch (IOException ex) {
            throw new CustomExceptions.BadRequestException("Cannot call Gemini API: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CustomExceptions.BadRequestException("Gemini API request was interrupted");
        }
    }

    private String buildEndpoint() {
        String baseUrl = aiProperties.getGemini().getBaseUrl();
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String configuredModel = aiProperties.getModel().startsWith("models/")
                ? aiProperties.getModel().substring("models/".length())
                : aiProperties.getModel();
        String model = URLEncoder.encode(configuredModel, StandardCharsets.UTF_8);
        return normalizedBaseUrl + "/" + model + ":generateContent";
    }

    private String extractText(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode promptFeedback = root.path("promptFeedback").path("blockReason");
        if (!promptFeedback.isMissingNode() && !promptFeedback.asText().isBlank()) {
            throw new CustomExceptions.BadRequestException("Gemini blocked the prompt: " + promptFeedback.asText());
        }

        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        List<String> chunks = new ArrayList<>();
        if (parts.isArray()) {
            for (JsonNode part : parts) {
                String text = part.path("text").asText();
                if (!text.isBlank()) {
                    chunks.add(text);
                }
            }
        }
        return String.join("", chunks).trim();
    }

    private void validateConfig() {
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            throw new CustomExceptions.BadRequestException("Missing GEMINI_API_KEY for Gemini provider");
        }
        if (aiProperties.getModel() == null || aiProperties.getModel().isBlank() || "mock".equalsIgnoreCase(aiProperties.getModel())) {
            throw new CustomExceptions.BadRequestException("Missing ai.model for Gemini provider");
        }
    }

    private Map<String, Object> examResponseSchema() {
        Map<String, Object> questionSchema = new LinkedHashMap<>();
        questionSchema.put("type", "OBJECT");
        questionSchema.put("properties", Map.of(
                "type", Map.of("type", "STRING", "enum", List.of("MULTIPLE_CHOICE", "ESSAY", "LAB_APPLICATION")),
                "question", Map.of("type", "STRING"),
                "options", Map.of("type", "ARRAY", "items", Map.of("type", "STRING")),
                "answer", Map.of("type", "STRING"),
                "explanation", Map.of("type", "STRING"),
                "topic", Map.of("type", "STRING")
        ));
        questionSchema.put("required", List.of("type", "question", "options", "answer", "explanation", "topic"));

        Map<String, Object> answerKeySchema = new LinkedHashMap<>();
        answerKeySchema.put("type", "OBJECT");
        answerKeySchema.put("properties", Map.of(
                "questionIndex", Map.of("type", "INTEGER"),
                "answer", Map.of("type", "STRING"),
                "explanation", Map.of("type", "STRING")
        ));
        answerKeySchema.put("required", List.of("questionIndex", "answer", "explanation"));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "OBJECT");
        schema.put("properties", Map.of(
                "title", Map.of("type", "STRING"),
                "durationMinutes", Map.of("type", "INTEGER"),
                "questions", Map.of("type", "ARRAY", "items", questionSchema),
                "answerKey", Map.of("type", "ARRAY", "items", answerKeySchema)
        ));
        schema.put("required", List.of("title", "durationMinutes", "questions", "answerKey"));
        return schema;
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
