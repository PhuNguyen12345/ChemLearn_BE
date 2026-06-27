package com.example.chemlearn.ai.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiProviderClient implements AiProviderClient {
    @Override
    public String chat(String prompt) {
        return """
                Mình đang dùng Mock AI nên chưa tốn chi phí API.

                **Cách làm nhanh**

                1. Xác định chất hoặc hiện tượng chính trong đề.
                2. Viết công thức/phương trình cần dùng bằng kí hiệu chuẩn, ví dụ:

                $$n = \\frac{m}{M}$$

                3. Thay số cẩn thận, chốt kết quả. Tin chuẩn em nhé.

                ===TTS_EXPLANATION===
                Khi gặp một câu hỏi Hóa học, em đọc đề thật chậm để tìm từ khóa chính trước. Sau đó em nối từ khóa đó với bài học đang học, ví dụ phản ứng hóa học, chất mới, số mol hoặc phương trình hóa học. Làm theo ba bước này là ngon luôn.
                """;
    }

    @Override
    public String chatWithImage(String prompt, String mimeType, byte[] imageBytes) {
        return """
                Mình đang dùng Mock AI nên chưa đọc ảnh thật.
                Khi bật Gemini, em có thể chụp ảnh đề bài và gửi lên đây.
                Bi sẽ đọc đề, nhận diện chủ đề và trình bày lời giải có công thức đẹp như:

                $$R + 2HCl \\rightarrow RCl_2 + H_2$$

                ===TTS_EXPLANATION===
                Với ảnh chụp đề bài, Bi sẽ cố gắng đọc dữ kiện trong ảnh trước. Nếu ảnh rõ, Bi sẽ tóm tắt đề, chỉ ra kiến thức cần dùng, rồi hướng dẫn từng bước giải. Nếu ảnh bị mờ hoặc thiếu dữ kiện, Bi sẽ nhắc em chụp lại rõ hơn để tránh giải sai.
                """;
    }

    @Override
    public String generateExam(String prompt) {
        return """
                {
                  "title": "Đề ôn tập KHTN/Hóa học THCS - Mock",
                  "durationMinutes": 45,
                  "questions": [
                    {
                      "type": "MULTIPLE_CHOICE",
                      "question": "Dấu hiệu nào thường cho thấy đã xảy ra phản ứng hóa học?",
                      "options": ["Chất chỉ thay đổi hình dạng", "Xuất hiện chất khí hoặc kết tủa", "Nước đá tan thành nước", "Cắt nhỏ một mẩu giấy"],
                      "answer": "Xuất hiện chất khí hoặc kết tủa",
                      "explanation": "Phản ứng hóa học tạo chất mới, thường có dấu hiệu như khí, kết tủa, đổi màu hoặc tỏa nhiệt.",
                      "topic": "Phản ứng hóa học"
                    },
                    {
                      "type": "ESSAY",
                      "question": "Hãy phân biệt biến đổi vật lí và biến đổi hóa học bằng một ví dụ an toàn trong đời sống.",
                      "options": [],
                      "answer": "Biến đổi vật lí không tạo chất mới, ví dụ nước đá tan. Biến đổi hóa học tạo chất mới, ví dụ đốt nến.",
                      "explanation": "Điểm mấu chốt là có hay không có chất mới được tạo thành.",
                      "topic": "Biến đổi chất"
                    },
                    {
                      "type": "LAB_APPLICATION",
                      "question": "Trong lab ảo, khi trộn hai dung dịch thấy xuất hiện kết tủa, em nên ghi lại những quan sát nào?",
                      "options": [],
                      "answer": "Màu sắc trước và sau khi trộn, thời điểm xuất hiện kết tủa, lượng kết tủa và điều kiện thí nghiệm.",
                      "explanation": "Ghi quan sát đầy đủ giúp kết luận hiện tượng chính xác và an toàn.",
                      "topic": "Quan sát thí nghiệm"
                    }
                  ],
                  "answerKey": [
                    {
                      "questionIndex": 1,
                      "answer": "Xuất hiện chất khí hoặc kết tủa",
                      "explanation": "Đây là dấu hiệu thường gặp khi có chất mới tạo thành."
                    },
                    {
                      "questionIndex": 2,
                      "answer": "Biến đổi vật lí không tạo chất mới; biến đổi hóa học tạo chất mới.",
                      "explanation": "So sánh dựa trên sự tạo thành chất mới."
                    },
                    {
                      "questionIndex": 3,
                      "answer": "Ghi màu sắc, hiện tượng kết tủa, thời điểm, lượng và điều kiện.",
                      "explanation": "Đây là các dữ kiện quan sát quan trọng trong lab."
                    }
                  ]
                }
                """;
    }

    @Override
    public byte[] synthesizeSpeech(String text) {
        return silentWav(400);
    }

    private byte[] silentWav(int durationMs) {
        int sampleRate = 24000;
        int channels = 1;
        int bitsPerSample = 16;
        int dataSize = sampleRate * channels * (bitsPerSample / 8) * durationMs / 1000;
        byte[] wav = new byte[44 + dataSize];
        writeAscii(wav, 0, "RIFF");
        writeIntLE(wav, 4, 36 + dataSize);
        writeAscii(wav, 8, "WAVE");
        writeAscii(wav, 12, "fmt ");
        writeIntLE(wav, 16, 16);
        writeShortLE(wav, 20, 1);
        writeShortLE(wav, 22, channels);
        writeIntLE(wav, 24, sampleRate);
        writeIntLE(wav, 28, sampleRate * channels * bitsPerSample / 8);
        writeShortLE(wav, 32, channels * bitsPerSample / 8);
        writeShortLE(wav, 34, bitsPerSample);
        writeAscii(wav, 36, "data");
        writeIntLE(wav, 40, dataSize);
        return wav;
    }

    private void writeAscii(byte[] target, int offset, String value) {
        byte[] bytes = value.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
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
}
