package com.example.chemlearn.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private String provider = "mock";
    private boolean cacheEnabled = true;
    private boolean fallbackEnabled = true;
    private String model = "mock";
    private String apiKey;
    private Double temperature = 0.4;
    private Integer requestTimeoutSeconds = 60;
    private String appTitle = "ChemLearn";
    private String appUrl = "http://localhost:5173";
    private OpenRouter openrouter = new OpenRouter();
    private Gemini gemini = new Gemini();
    private Tts tts = new Tts();

    @Data
    public static class OpenRouter {
        private String baseUrl = "https://openrouter.ai/api/v1/chat/completions";
    }

    @Data
    public static class Gemini {
        private String baseUrl = "https://generativelanguage.googleapis.com/v1beta/models";
        private String fallbackModel = "gemini-2.5-flash-lite";
    }

    @Data
    public static class Tts {
        private String model = "gemini-2.5-flash-preview-tts";
        private String voiceName = "Achird";
        private Integer requestTimeoutSeconds = 60;
        private Integer maxChars = 30000;
        private Integer chunkChars = 5000;
        private Integer maxChunks = 6;
    }
}
