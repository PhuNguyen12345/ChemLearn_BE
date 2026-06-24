package com.example.chemlearn.ai.controller;

import com.example.chemlearn.ai.dto.*;
import com.example.chemlearn.ai.enums.BookType;
import com.example.chemlearn.ai.service.AiService;
import com.example.chemlearn.core.shared.constants.ApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.API_VERSION + "/ai")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;

    @PostMapping("/chat")
    public AiChatResponse chat(@Valid @RequestBody AiChatRequest request) {
        return aiService.chat(request);
    }

    @PostMapping(value = "/tts", produces = "audio/wav")
    public ResponseEntity<byte[]> synthesizeSpeech(@Valid @RequestBody AiTtsRequest request) {
        byte[] audio = aiService.synthesizeSpeech(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(audio);
    }

    @PostMapping(value = "/chat-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AiChatResponse chatWithImage(@RequestParam UUID studentId,
                                        @RequestParam(required = false) UUID sessionId,
                                        @RequestParam Integer grade,
                                        @RequestParam BookType bookType,
                                        @RequestParam(required = false) String message,
                                        @RequestParam MultipartFile image) {
        return aiService.chatWithImage(studentId, sessionId, grade, bookType, message, image);
    }

    @PostMapping("/generate-exam")
    public GenerateExamResponse generateExam(@Valid @RequestBody GenerateExamRequest request) {
        return aiService.generateExam(request);
    }

    @PostMapping("/submit-generated-exam")
    public SubmitGeneratedExamResponse submitGeneratedExam(@Valid @RequestBody SubmitGeneratedExamRequest request) {
        return aiService.submitGeneratedExam(request);
    }

    @PostMapping("/analyze-result")
    public AnalyzeResultResponse analyzeResult(@Valid @RequestBody AnalyzeResultRequest request) {
        return aiService.analyzeResult(request);
    }

    @GetMapping("/sessions/{studentId}")
    public List<AiChatSessionResponse> getSessions(@PathVariable UUID studentId) {
        return aiService.getSessions(studentId);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public List<AiChatMessageResponse> getMessages(@PathVariable UUID sessionId) {
        return aiService.getMessages(sessionId);
    }

    @GetMapping("/generated-exams/by-student/{studentId}")
    public List<GeneratedExamSummaryResponse> getGeneratedExams(@PathVariable UUID studentId) {
        return aiService.getGeneratedExams(studentId);
    }

    @GetMapping("/generated-exams/{examId}")
    public GenerateExamResponse getGeneratedExam(@PathVariable UUID examId) {
        return aiService.getGeneratedExam(examId);
    }
}
