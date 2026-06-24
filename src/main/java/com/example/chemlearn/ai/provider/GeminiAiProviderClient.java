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

    @Override
    public byte[] synthesizeSpeech(String text) {
        validateConfig();
        if (text == null || text.isBlank()) {
            throw new CustomExceptions.BadRequestException("TTS text is required");
        }

        try {
            Map<String, Object> generationConfig = new LinkedHashMap<>();
            generationConfig.put("responseModalities", List.of("AUDIO"));
            generationConfig.put("speechConfig", Map.of(
                    "voiceConfig", Map.of(
                            "prebuiltVoiceConfig", Map.of(
                                    "voiceName", aiProperties.getTts().getVoiceName()
                            )
                    )
            ));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("contents", List.of(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", buildTtsPrompt(text)))
            )));
            body.put("generationConfig", generationConfig);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(buildTtsEndpoint()))
                    .timeout(Duration.ofSeconds(aiProperties.getTts().getRequestTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", aiProperties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(aiProperties.getTts().getRequestTimeoutSeconds()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new CustomExceptions.BadRequestException("Gemini TTS API error: " + response.statusCode() + " - " + extractError(response.body()));
            }

            AudioData audioData = extractAudioData(response.body());
            if (audioData.mimeType().toLowerCase().contains("wav")) {
                return audioData.bytes();
            }
            if (audioData.mimeType().toLowerCase().contains("audio/l16")) {
                return wrapPcmAsWav(audioData.bytes(), sampleRateFromMimeType(audioData.mimeType()), 1, 16);
            }
            return audioData.bytes();
        } catch (IOException ex) {
            throw new CustomExceptions.BadRequestException("Cannot call Gemini TTS API: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CustomExceptions.BadRequestException("Gemini TTS API request was interrupted");
        }
    }

    private String requestGenerateContent(String prompt, boolean jsonMode, double fallbackTemperature) {
        return requestGenerateContent(List.of(Map.of("text", prompt)), jsonMode, fallbackTemperature);
    }

    private String requestGenerateContent(List<Map<String, Object>> parts, boolean jsonMode, double fallbackTemperature) {
        validateConfig();

        String primaryModel = normalizeModelName(aiProperties.getModel());
        try {
            return requestGenerateContentWithModel(parts, jsonMode, fallbackTemperature, primaryModel);
        } catch (RuntimeException ex) {
            String fallbackModel = normalizeModelName(aiProperties.getGemini().getFallbackModel());
            if (!isRetryableGeminiError(ex) || fallbackModel.isBlank() || fallbackModel.equals(primaryModel)) {
                throw ex;
            }
            return requestGenerateContentWithModel(parts, jsonMode, fallbackTemperature, fallbackModel);
        }
    }

    private String requestGenerateContentWithModel(List<Map<String, Object>> parts,
                                                   boolean jsonMode,
                                                   double fallbackTemperature,
                                                   String modelName) {
        try {
            Map<String, Object> generationConfig = new LinkedHashMap<>();
            generationConfig.put("temperature", aiProperties.getTemperature() == null ? fallbackTemperature : aiProperties.getTemperature());
            generationConfig.put("maxOutputTokens", 8192);
            if (modelName.contains("2.5")) {
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
                    .uri(URI.create(buildEndpoint(modelName)))
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

    private String buildEndpoint(String modelName) {
        String baseUrl = aiProperties.getGemini().getBaseUrl();
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String model = URLEncoder.encode(modelName, StandardCharsets.UTF_8);
        return normalizedBaseUrl + "/" + model + ":generateContent";
    }

    private String normalizeModelName(String model) {
        if (model == null) {
            return "";
        }
        String normalized = model.trim();
        if (normalized.startsWith("models/")) {
            normalized = normalized.substring("models/".length());
        }
        if (normalized.startsWith("google/")) {
            normalized = normalized.substring("google/".length());
        }
        if ("gemini-flash-1.5".equalsIgnoreCase(normalized)) {
            return "gemini-1.5-flash";
        }
        if ("gemini-pro-1.5".equalsIgnoreCase(normalized)) {
            return "gemini-1.5-pro";
        }
        return normalized;
    }

    private boolean isRetryableGeminiError(RuntimeException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        return message.contains("429")
                || message.contains("500")
                || message.contains("502")
                || message.contains("503")
                || message.contains("504")
                || message.contains("rate")
                || message.contains("quota")
                || message.contains("overload")
                || message.contains("unavailable")
                || message.contains("timeout")
                || message.contains("timed out");
    }

    private String buildTtsEndpoint() {
        String baseUrl = aiProperties.getGemini().getBaseUrl();
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String configuredModel = aiProperties.getTts().getModel().startsWith("models/")
                ? aiProperties.getTts().getModel().substring("models/".length())
                : aiProperties.getTts().getModel();
        String model = URLEncoder.encode(configuredModel, StandardCharsets.UTF_8);
        return normalizedBaseUrl + "/" + model + ":generateContent";
    }

    private String buildTtsPrompt(String text) {
        return """
                Đọc đoạn sau bằng giọng người Việt tự nhiên, thân thiện, rõ ràng, giống một gia sư Hóa/KHTN trẻ đang giải thích cho học sinh cấp 2.
                Nhịp đọc vừa phải, ấm áp, có năng lượng. Các công thức hóa học đọc theo cách học sinh Việt Nam dễ hiểu.
                Nếu trong nội dung có cụm trend như "tin chuẩn em nhé", "là ngon luôn", "thế mà lại hay", "mời đoàn mình di chuyển đến phần tiếp theo", hãy đọc tự nhiên, vui vừa phải, không làm quá.
                Có thể thêm tiếng cười nhẹ "ha ha" sau một ý vui hoặc khi chuyển phần, tối đa 2-3 lần trong cả đoạn; không cười liên tục và không làm mất sự nghiêm túc của lời giải.
                Đọc tên chất/nguyên tố theo cách gọi của chương trình mới khi phù hợp: hydrogen, oxygen, chlorine, hydrochloric acid, iron(II) chloride... Nếu nội dung dùng công thức, đọc rõ chỉ số như H hai, O hai, C O hai, H C lờ, Fe Cl hai.
                Không tự đổi sang cách gọi cũ như hiđro/oxi/clo nếu nội dung đang dùng danh pháp mới hoặc công thức hiện đại. Với "đktc/điều kiện chuẩn" theo chương trình mới, đọc là 25 độ C và 1 bar, thể tích mol khí 24 phẩy 79 lít trên mol; không đọc thành 22 phẩy 4 trừ khi nội dung nói rõ 0 độ C, 1 atm hoặc quy ước cũ.

                Nội dung cần đọc:
                %s
                """.formatted(text.trim());
    }

    private AudioData extractAudioData(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        if (parts.isArray()) {
            for (JsonNode part : parts) {
                JsonNode inlineData = part.path("inlineData");
                String base64Data = inlineData.path("data").asText();
                if (!base64Data.isBlank()) {
                    String mimeType = inlineData.path("mimeType").asText("audio/wav");
                    return new AudioData(Base64.getDecoder().decode(base64Data), mimeType);
                }
            }
        }
        throw new CustomExceptions.BadRequestException("Gemini TTS returned an empty audio response");
    }

    private int sampleRateFromMimeType(String mimeType) {
        if (mimeType == null) {
            return 24000;
        }
        String marker = "rate=";
        int markerIndex = mimeType.toLowerCase().indexOf(marker);
        if (markerIndex < 0) {
            return 24000;
        }
        int start = markerIndex + marker.length();
        int end = start;
        while (end < mimeType.length() && Character.isDigit(mimeType.charAt(end))) {
            end++;
        }
        try {
            return Integer.parseInt(mimeType.substring(start, end));
        } catch (NumberFormatException ignored) {
            return 24000;
        }
    }

    private byte[] wrapPcmAsWav(byte[] pcm, int sampleRate, int channels, int bitsPerSample) {
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;
        int dataSize = pcm.length;
        int fileSize = 36 + dataSize;
        byte[] wav = new byte[44 + dataSize];

        writeAscii(wav, 0, "RIFF");
        writeIntLE(wav, 4, fileSize);
        writeAscii(wav, 8, "WAVE");
        writeAscii(wav, 12, "fmt ");
        writeIntLE(wav, 16, 16);
        writeShortLE(wav, 20, 1);
        writeShortLE(wav, 22, channels);
        writeIntLE(wav, 24, sampleRate);
        writeIntLE(wav, 28, byteRate);
        writeShortLE(wav, 32, blockAlign);
        writeShortLE(wav, 34, bitsPerSample);
        writeAscii(wav, 36, "data");
        writeIntLE(wav, 40, dataSize);
        System.arraycopy(pcm, 0, wav, 44, dataSize);
        return wav;
    }

    private void writeAscii(byte[] target, int offset, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(bytes, 0, target, offset, bytes.length);
    }

    private void writeIntLE(byte[] target, int offset, int value) {
        target[offset] = (byte) (value & 0xff);
        target[offset + 1] = (byte) ((value >> 8) & 0xff);
        target[offset + 2] = (byte) ((value >> 16) & 0xff);
        target[offset + 3] = (byte) ((value >> 24) & 0xff);
    }

    private void writeShortLE(byte[] target, int offset, int value) {
        target[offset] = (byte) (value & 0xff);
        target[offset + 1] = (byte) ((value >> 8) & 0xff);
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

    private record AudioData(byte[] bytes, String mimeType) {
    }
}
