package com.example.chemlearn.ai.provider;

public interface AiProviderClient {
    String chat(String prompt);

    String chatWithImage(String prompt, String mimeType, byte[] imageBytes);

    String generateExam(String prompt);

    byte[] synthesizeSpeech(String text);
}
