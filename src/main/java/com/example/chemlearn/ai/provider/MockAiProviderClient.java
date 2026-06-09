package com.example.chemlearn.ai.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiProviderClient implements AiProviderClient {
    @Override
    public String chat(String prompt) {
        return """
                Mình đang dùng Mock AI nên chưa tốn chi phí API. Với nội dung hiện tại, em hãy nhớ 3 bước:
                1. Xác định khái niệm chính trong câu hỏi.
                2. Liên hệ với bài học KHTN/Hóa học đang học.
                3. Dùng ví dụ an toàn, quen thuộc để kiểm tra lại hiểu biết.

                Nếu câu hỏi liên quan đến thí nghiệm, em nên mở lab được gợi ý để quan sát hiện tượng trước rồi quay lại giải thích bằng lời của mình.
                """;
    }

    @Override
    public String chatWithImage(String prompt, String mimeType, byte[] imageBytes) {
        return """
                Mình đang dùng Mock AI nên chưa đọc ảnh thật. Khi bật Gemini, em có thể chụp ảnh đề bài và gửi lên đây.

                Cách ChemAI sẽ hỗ trợ:
                1. Đọc nội dung đề trong ảnh.
                2. Nhận diện chủ đề KHTN/Hóa học phù hợp.
                3. Hướng dẫn từng bước giải, kèm nhắc lại kiến thức cần dùng.

                Nếu ảnh mờ hoặc thiếu đề, ChemAI sẽ nhắc em chụp lại rõ hơn.
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
}
