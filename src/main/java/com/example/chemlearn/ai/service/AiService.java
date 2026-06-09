package com.example.chemlearn.ai.service;

import com.example.chemlearn.ai.config.AiProperties;
import com.example.chemlearn.ai.dto.*;
import com.example.chemlearn.ai.entity.*;
import com.example.chemlearn.ai.enums.*;
import com.example.chemlearn.ai.provider.AiProviderClient;
import com.example.chemlearn.ai.repository.*;
import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.lab.entity.Lab;
import com.example.chemlearn.lab.repository.LabRepository;
import com.example.chemlearn.lms.entity.Lesson;
import com.example.chemlearn.lms.enums.MaterialScope;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.LessonRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {
    private static final int MAX_CONTEXT_LESSONS = 4;
    private static final int MAX_RECOMMENDATIONS = 4;
    private static final long MAX_CHAT_IMAGE_BYTES = 8L * 1024 * 1024;

    private final AiProviderClient aiProviderClient;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;
    private final LabRepository labRepository;
    private final CurriculumLessonRepository curriculumLessonRepository;
    private final AiChatSessionRepository aiChatSessionRepository;
    private final AiChatMessageRepository aiChatMessageRepository;
    private final AiResponseCacheRepository aiResponseCacheRepository;
    private final StudentTopicMasteryRepository studentTopicMasteryRepository;
    private final AiGeneratedExamRepository aiGeneratedExamRepository;

    @Transactional
    public AiChatResponse chat(AiChatRequest request) {
        Student student = getAuthorizedStudent(request.getStudentId());
        String chatTopic = resolveChatTopic(request);
        AiChatSession session = resolveSession(request.getSessionId(), student, request.getGrade(), request.getBookType(), chatTopic);
        chatTopic = ensureSessionTopic(session, chatTopic);
        saveMessage(session, AiMessageRole.USER, request.getMessage());

        String normalizedQuestion = normalizeForCache(request.getMessage());
        String cacheKey = buildCacheKey(request.getGrade(), request.getBookType(), chatTopic, normalizedQuestion);

        if (aiProperties.isCacheEnabled()) {
            Optional<AiResponseCache> cached = aiResponseCacheRepository.findByCacheKey(cacheKey);
            if (cached.isPresent()) {
                AiResponseCache cache = cached.get();
                List<SuggestedLabDTO> cachedLabs = toSuggestedLabs(cache.getSuggestedLabs());
                String cachedAnswer = sanitizeAiAnswer(cache.getAnswer());
                saveMessage(session, AiMessageRole.ASSISTANT, cachedAnswer);
                touchSession(session);
                return AiChatResponse.builder()
                        .sessionId(session.getId())
                        .topic(chatTopic)
                        .answer(cachedAnswer)
                        .suggestedLabs(cachedLabs)
                        .build();
            }
        }

        String curriculumContext = buildCurriculumContext(request.getGrade(), request.getBookType(), chatTopic);
        List<SuggestedLabDTO> suggestedLabs = findSuggestedLabs(request.getGrade(), request.getBookType(), chatTopic);
        String prompt = buildChatPrompt(request, chatTopic, curriculumContext, suggestedLabs);
        String answer = sanitizeAiAnswer(aiProviderClient.chat(prompt));

        saveMessage(session, AiMessageRole.ASSISTANT, answer);
        touchSession(session);

        if (aiProperties.isCacheEnabled()) {
            AiResponseCache cache = new AiResponseCache();
            cache.setCacheKey(cacheKey);
            cache.setNormalizedQuestion(normalizedQuestion);
            cache.setGrade(request.getGrade());
            cache.setBookType(request.getBookType());
            cache.setTopic(chatTopic);
            cache.setAnswer(answer);
            cache.setSuggestedLabs(toCacheLabs(suggestedLabs));
            aiResponseCacheRepository.save(cache);
        }

        return AiChatResponse.builder()
                .sessionId(session.getId())
                .topic(chatTopic)
                .answer(answer)
                .suggestedLabs(suggestedLabs)
                .build();
    }

    @Transactional
    public AiChatResponse chatWithImage(UUID studentId,
                                        UUID sessionId,
                                        Integer grade,
                                        BookType bookType,
                                        String message,
                                        MultipartFile image) {
        Student student = getAuthorizedStudent(studentId);
        validateGradeAndBookType(grade, bookType);
        String mimeType = validateChatImage(image);
        byte[] imageBytes = readImageBytes(image);

        String userPrompt = blankToNull(message) == null
                ? "Hãy đọc đề trong ảnh và hướng dẫn em cách giải."
                : message.trim();
        String chatTopic = resolveImageChatTopic(userPrompt);
        AiChatSession session = resolveSession(sessionId, student, grade, bookType, chatTopic);
        chatTopic = ensureSessionTopic(session, chatTopic);

        String visibleUserMessage = userPrompt + "\n[Đã gửi ảnh đề bài: " + safeFilename(image.getOriginalFilename()) + "]";
        saveMessage(session, AiMessageRole.USER, visibleUserMessage);

        String imageHash = sha256Hex(imageBytes);
        String normalizedQuestion = normalizeForCache(userPrompt) + " image " + imageHash;
        String cacheKey = buildCacheKey(grade, bookType, chatTopic, normalizedQuestion);

        if (aiProperties.isCacheEnabled()) {
            Optional<AiResponseCache> cached = aiResponseCacheRepository.findByCacheKey(cacheKey);
            if (cached.isPresent()) {
                AiResponseCache cache = cached.get();
                List<SuggestedLabDTO> cachedLabs = toSuggestedLabs(cache.getSuggestedLabs());
                String cachedAnswer = sanitizeAiAnswer(cache.getAnswer());
                saveMessage(session, AiMessageRole.ASSISTANT, cachedAnswer);
                touchSession(session);
                return AiChatResponse.builder()
                        .sessionId(session.getId())
                        .topic(chatTopic)
                        .answer(cachedAnswer)
                        .suggestedLabs(cachedLabs)
                        .build();
            }
        }

        String curriculumContext = buildCurriculumContext(grade, bookType, chatTopic);
        List<SuggestedLabDTO> suggestedLabs = findSuggestedLabs(grade, bookType, chatTopic);
        String prompt = buildImageChatPrompt(grade, bookType, chatTopic, userPrompt, curriculumContext, suggestedLabs);
        String answer = sanitizeAiAnswer(aiProviderClient.chatWithImage(prompt, mimeType, imageBytes));

        saveMessage(session, AiMessageRole.ASSISTANT, answer);
        touchSession(session);

        if (aiProperties.isCacheEnabled()) {
            AiResponseCache cache = new AiResponseCache();
            cache.setCacheKey(cacheKey);
            cache.setNormalizedQuestion(normalizedQuestion);
            cache.setGrade(grade);
            cache.setBookType(bookType);
            cache.setTopic(chatTopic);
            cache.setAnswer(answer);
            cache.setSuggestedLabs(toCacheLabs(suggestedLabs));
            aiResponseCacheRepository.save(cache);
        }

        return AiChatResponse.builder()
                .sessionId(session.getId())
                .topic(chatTopic)
                .answer(answer)
                .suggestedLabs(suggestedLabs)
                .build();
    }

    @Transactional
    public GenerateExamResponse generateExam(GenerateExamRequest request) {
        Student student = getAuthorizedStudent(request.getStudentId());
        String curriculumContext = buildCurriculumContext(request.getGrade(), request.getBookType(), request.getTopic());
        String prompt = buildGenerateExamPrompt(request, curriculumContext);
        GenerateExamResponse response = parseAndValidateExam(aiProviderClient.generateExam(prompt));

        AiGeneratedExam exam = new AiGeneratedExam();
        exam.setStudent(student);
        exam.setGrade(request.getGrade());
        exam.setBookType(request.getBookType());
        exam.setExamType(request.getExamType());
        exam.setTopic(cleanTopic(request.getTopic()));
        exam.setDifficulty(request.getDifficulty());
        exam.setTitle(response.getTitle());
        exam.setDurationMinutes(response.getDurationMinutes());
        exam.setQuestions(objectMapper.convertValue(response.getQuestions(), new TypeReference<>() {}));
        exam.setAnswerKey(objectMapper.convertValue(response.getAnswerKey(), new TypeReference<>() {}));
        AiGeneratedExam savedExam = aiGeneratedExamRepository.save(exam);
        response.setExamId(savedExam.getId());

        return response;
    }

    @Transactional
    public SubmitGeneratedExamResponse submitGeneratedExam(SubmitGeneratedExamRequest request) {
        Student student = getAuthorizedStudent(request.getStudentId());
        AiGeneratedExam exam = aiGeneratedExamRepository.findByIdAndStudent_Id(request.getExamId(), student.getId())
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Generated exam not found"));

        List<GeneratedQuestionDTO> questions = objectMapper.convertValue(
                exam.getQuestions(),
                new TypeReference<List<GeneratedQuestionDTO>>() {}
        );

        Map<Integer, String> answerMap = request.getAnswers().stream()
                .collect(Collectors.toMap(
                        SubmitExamAnswerDTO::getQuestionIndex,
                        answer -> answer.getAnswer() == null ? "" : answer.getAnswer(),
                        (left, right) -> right
                ));

        List<GradedQuestionDTO> gradedQuestions = new ArrayList<>();
        List<String> wrongTopics = new ArrayList<>();
        int autoGradedTotal = 0;
        int correct = 0;

        for (int i = 0; i < questions.size(); i++) {
            GeneratedQuestionDTO question = questions.get(i);
            int questionIndex = i + 1;
            String studentAnswer = answerMap.getOrDefault(questionIndex, "");
            boolean autoGraded = isAutoGradable(question);
            Boolean isCorrect = null;

            if (autoGraded) {
                autoGradedTotal++;
                isCorrect = gradeAnswer(question, studentAnswer);
                if (Boolean.TRUE.equals(isCorrect)) {
                    correct++;
                } else {
                    wrongTopics.add(cleanTopic(question.getTopic()));
                }
            }

            gradedQuestions.add(GradedQuestionDTO.builder()
                    .questionIndex(questionIndex)
                    .correct(isCorrect)
                    .autoGraded(autoGraded)
                    .studentAnswer(studentAnswer)
                    .expectedAnswer(question.getAnswer())
                    .explanation(question.getExplanation())
                    .topic(cleanTopic(question.getTopic()))
                    .build());
        }

        int total = Math.max(autoGradedTotal, 1);
        AnalyzeResultRequest analysisRequest = new AnalyzeResultRequest();
        analysisRequest.setStudentId(student.getId());
        analysisRequest.setGrade(exam.getGrade());
        analysisRequest.setBookType(exam.getBookType());
        analysisRequest.setTopic(exam.getTopic());
        analysisRequest.setCorrect(correct);
        analysisRequest.setTotal(total);
        analysisRequest.setWrongTopics(wrongTopics.stream().distinct().toList());
        AnalyzeResultResponse analysis = analyzeResult(analysisRequest);

        return SubmitGeneratedExamResponse.builder()
                .correct(correct)
                .total(total)
                .scorePercent((int) Math.round((correct * 100.0) / total))
                .gradedQuestions(gradedQuestions)
                .analysis(analysis)
                .build();
    }

    @Transactional
    public AnalyzeResultResponse analyzeResult(AnalyzeResultRequest request) {
        Student student = getAuthorizedStudent(request.getStudentId());
        if (request.getCorrect() > request.getTotal()) {
            throw new CustomExceptions.BadRequestException("correct must be less than or equal to total");
        }

        double accuracy = request.getCorrect() / (double) request.getTotal();
        MasteryLevel masteryLevel = calculateMasteryLevel(accuracy);
        String topic = cleanTopic(request.getTopic());
        List<String> wrongTopics = sanitizeTopics(request.getWrongTopics());

        StudentTopicMastery mastery = studentTopicMasteryRepository
                .findExisting(student.getId(), request.getGrade(), request.getBookType(), topic)
                .orElseGet(StudentTopicMastery::new);
        mastery.setStudent(student);
        mastery.setGrade(request.getGrade());
        mastery.setBookType(request.getBookType());
        mastery.setTopic(topic);
        mastery.setCorrectCount(request.getCorrect());
        mastery.setTotalCount(request.getTotal());
        mastery.setLastAccuracy(accuracy);
        mastery.setMasteryLevel(masteryLevel);
        mastery.setWrongTopics(wrongTopics);
        studentTopicMasteryRepository.save(mastery);

        List<RecommendedLessonDTO> lessons = findRecommendedLessons(request.getGrade(), request.getBookType(), topic, wrongTopics);

        return AnalyzeResultResponse.builder()
                .masteryLevel(masteryLevel)
                .strengths(buildStrengths(masteryLevel, topic, accuracy))
                .weaknesses(buildWeaknesses(masteryLevel, topic, wrongTopics))
                .recommendedLessons(lessons)
                .recommendedLabs(List.of())
                .build();
    }

    @Transactional
    public List<AiChatSessionResponse> getSessions(UUID studentId) {
        getAuthorizedStudent(studentId);
        return aiChatSessionRepository.findByStudent_IdOrderByUpdatedAtDesc(studentId).stream()
                .map(this::toSessionResponse)
                .toList();
    }

    @Transactional
    public List<AiChatMessageResponse> getMessages(UUID sessionId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        AiChatSession session = aiChatSessionRepository.findByIdAndStudent_Id(sessionId, currentUserId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("AI chat session not found"));
        return aiChatMessageRepository.findBySession_IdOrderByCreatedAtAsc(session.getId()).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional
    public List<GeneratedExamSummaryResponse> getGeneratedExams(UUID studentId) {
        getAuthorizedStudent(studentId);
        return aiGeneratedExamRepository.findByStudent_IdOrderByCreatedAtDesc(studentId).stream()
                .map(this::toGeneratedExamSummary)
                .toList();
    }

    @Transactional
    public GenerateExamResponse getGeneratedExam(UUID examId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        AiGeneratedExam exam = aiGeneratedExamRepository.findByIdAndStudent_Id(examId, currentUserId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Generated exam not found"));
        return toGenerateExamResponse(exam);
    }

    private Student getAuthorizedStudent(UUID studentId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (!currentUserId.equals(studentId)) {
            throw new CustomExceptions.UnauthorizedException("You can only access your own AI Tutor data");
        }
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Student not found"));
    }

    private AiChatSession resolveSession(UUID sessionId, Student student, Integer grade, BookType bookType, String topic) {
        if (sessionId != null) {
            return aiChatSessionRepository.findByIdAndStudent_Id(sessionId, student.getId())
                    .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("AI chat session not found"));
        }

        AiChatSession session = new AiChatSession();
        session.setStudent(student);
        session.setGrade(grade);
        session.setBookType(bookType);
        session.setTopic(cleanTopic(topic));
        return aiChatSessionRepository.save(session);
    }

    private String resolveChatTopic(AiChatRequest request) {
        return inferTopicFromMessage(request.getMessage());
    }

    private String resolveImageChatTopic(String message) {
        String topic = inferTopicFromMessage(message);
        return isGenericTopic(topic) ? "Bài tập từ ảnh" : topic;
    }

    private String ensureSessionTopic(AiChatSession session, String fallbackTopic) {
        String currentTopic = cleanTopic(session.getTopic());
        if (!isGenericTopic(currentTopic)) {
            return currentTopic;
        }

        String topic = cleanTopic(fallbackTopic);
        if (!isGenericTopic(topic)) {
            session.setTopic(topic);
            aiChatSessionRepository.save(session);
            return topic;
        }
        return currentTopic;
    }

    private String inferTopicFromMessage(String message) {
        String normalized = normalizeForCache(message);
        if (normalized.contains("sat") || normalized.contains(" fe ") || normalized.startsWith("fe ")) {
            return "Sắt và hợp chất";
        }
        if (normalized.contains("phan ung") || normalized.contains("bien doi hoa")) {
            return "Phản ứng hóa học";
        }
        if (normalized.contains("mol") || normalized.contains("khoi luong") || normalized.contains("the tich khi")) {
            return "Tính toán hóa học";
        }
        if (normalized.contains("axit") || normalized.contains("acid") || normalized.contains("hcl")) {
            return "Axit và phản ứng";
        }
        if (normalized.contains("baz") || normalized.contains("kiem") || normalized.contains("naoh")) {
            return "Base và dung dịch kiềm";
        }
        if (normalized.contains("muoi") || normalized.contains("nacl")) {
            return "Muối và hợp chất ion";
        }
        if (normalized.contains("nguyen tu") || normalized.contains("electron") || normalized.contains("proton")) {
            return "Cấu tạo nguyên tử";
        }
        if (normalized.contains("lien ket")) {
            return "Liên kết hóa học";
        }
        if (normalized.contains("oxi") || normalized.contains("oxygen") || normalized.contains("o2")) {
            return "Oxygen và không khí";
        }
        if (normalized.contains("nuoc") || normalized.contains("h2o")) {
            return "Nước và dung dịch";
        }

        List<String> words = Arrays.stream(normalized.split("\\s+"))
                .filter(word -> word.length() > 2)
                .limit(5)
                .toList();
        if (words.isEmpty()) {
            return "Chủ đề chung";
        }
        return "Trao đổi: " + String.join(" ", words);
    }

    private boolean isGenericTopic(String topic) {
        String normalized = normalizeForCache(topic);
        return normalized.isBlank() || normalized.equals("chu de chung") || normalized.startsWith("trao doi");
    }

    private void validateGradeAndBookType(Integer grade, BookType bookType) {
        if (grade == null || grade < 6 || grade > 9) {
            throw new CustomExceptions.BadRequestException("grade must be between 6 and 9");
        }
        if (bookType == null) {
            throw new CustomExceptions.BadRequestException("bookType is required");
        }
    }

    private String validateChatImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new CustomExceptions.BadRequestException("Image is required");
        }
        if (image.getSize() > MAX_CHAT_IMAGE_BYTES) {
            throw new CustomExceptions.BadRequestException("Image must be 8MB or smaller");
        }

        String contentType = image.getContentType() == null ? "" : image.getContentType().toLowerCase(Locale.ROOT);
        if ("image/jpg".equals(contentType)) {
            return "image/jpeg";
        }
        if (!Set.of("image/jpeg", "image/png", "image/webp").contains(contentType)) {
            throw new CustomExceptions.BadRequestException("Only JPEG, PNG, or WebP images are supported");
        }
        return contentType;
    }

    private byte[] readImageBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException ex) {
            throw new CustomExceptions.BadRequestException("Cannot read uploaded image");
        }
    }

    private String safeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "anh-de-bai";
        }
        return filename.replaceAll("[\\\\/]+", "_");
    }

    private void saveMessage(AiChatSession session, AiMessageRole role, String content) {
        AiChatMessage message = new AiChatMessage();
        message.setSession(session);
        message.setRole(role);
        message.setContent(content);
        aiChatMessageRepository.save(message);
    }

    private void touchSession(AiChatSession session) {
        session.setUpdatedAt(Instant.now());
        aiChatSessionRepository.save(session);
    }

    private String buildCurriculumContext(Integer grade, BookType bookType, String topic) {
        String keyword = blankToNull(topic);
        List<CurriculumLesson> curriculumLessons = curriculumLessonRepository.searchContext(grade, bookType, keyword)
                .stream()
                .limit(MAX_CONTEXT_LESSONS)
                .toList();

        if (!curriculumLessons.isEmpty()) {
            return curriculumLessons.stream()
                    .map(lesson -> "- " + lesson.getTitle() + ": " + truncate(stripHtml(lesson.getContent()), 700))
                    .collect(Collectors.joining("\n"));
        }

        List<Lesson> lessons = searchLessons(grade, keyword);
        if (lessons.isEmpty() && keyword != null) {
            lessons = lessonRepository.findPublishedGlobalLessonsByGrade(grade, MaterialScope.GLOBAL);
        }

        if (lessons.isEmpty()) {
            return "Chưa có dữ liệu chương trình phù hợp trong database. Chỉ trả lời ở mức khái quát, không bịa nội dung.";
        }

        return lessons.stream()
                .limit(MAX_CONTEXT_LESSONS)
                .map(lesson -> "- " + lesson.getTitle() + ": " + truncate(stripHtml(lesson.getTextContent()), 700))
                .collect(Collectors.joining("\n"));
    }

    private List<Lesson> searchLessons(Integer grade, String keyword) {
        if (keyword == null) {
            return lessonRepository.findPublishedGlobalLessonsByGrade(grade, MaterialScope.GLOBAL);
        }
        return lessonRepository.searchPublishedGlobalLessonsByGradeAndKeyword(grade, MaterialScope.GLOBAL, keyword);
    }

    private List<SuggestedLabDTO> findSuggestedLabs(Integer grade, BookType bookType, String topic) {
        String keyword = blankToNull(topic);
        LinkedHashMap<UUID, SuggestedLabDTO> labs = new LinkedHashMap<>();

        curriculumLessonRepository.searchContext(grade, bookType, keyword).stream()
                .limit(MAX_RECOMMENDATIONS)
                .forEach(curriculumLesson -> {
                    if (curriculumLesson.getLab() != null) {
                        addLab(labs, curriculumLesson.getLab());
                    } else if (curriculumLesson.getLesson() != null && curriculumLesson.getLesson().getLab() != null) {
                        addLab(labs, curriculumLesson.getLesson().getLab());
                    }
                });

        if (labs.size() < MAX_RECOMMENDATIONS) {
            searchLessons(grade, keyword).stream()
                    .filter(lesson -> lesson.getLab() != null)
                    .forEach(lesson -> addLab(labs, lesson.getLab()));
        }

        if (labs.size() < MAX_RECOMMENDATIONS && keyword != null) {
            labRepository.searchPremadeLabsByKeyword(keyword, PageRequest.of(0, MAX_RECOMMENDATIONS))
                    .forEach(lab -> addLab(labs, lab));
        }

        return labs.values().stream().limit(MAX_RECOMMENDATIONS).toList();
    }

    private List<RecommendedLessonDTO> findRecommendedLessons(Integer grade, BookType bookType, String topic, List<String> wrongTopics) {
        LinkedHashMap<UUID, RecommendedLessonDTO> lessons = new LinkedHashMap<>();
        List<String> keywords = wrongTopics.isEmpty() ? List.of(topic) : wrongTopics;

        for (String keyword : keywords) {
            searchLessons(grade, blankToNull(keyword)).stream()
                    .forEach(lesson -> lessons.putIfAbsent(lesson.getId(), toRecommendedLesson(lesson)));
            if (lessons.size() >= MAX_RECOMMENDATIONS) {
                break;
            }
        }

        if (lessons.isEmpty()) {
            lessonRepository.findPublishedGlobalLessonsByGrade(grade, MaterialScope.GLOBAL).stream()
                    .limit(MAX_RECOMMENDATIONS)
                    .forEach(lesson -> lessons.putIfAbsent(lesson.getId(), toRecommendedLesson(lesson)));
        }

        return lessons.values().stream().limit(MAX_RECOMMENDATIONS).toList();
    }

    private String buildChatPrompt(AiChatRequest request, String topic, String curriculumContext, List<SuggestedLabDTO> labs) {
        String labContext = labs.isEmpty()
                ? "Không có lab ảo liên quan."
                : labs.stream().map(lab -> "- " + lab.getTitle()).collect(Collectors.joining("\n"));

        return """
                Bạn là ChemAI Tutor, trợ lý học tập môn KHTN/Hóa học THCS Việt Nam.

                Thông tin học sinh:
                - Lớp: %s
                - Bộ sách: %s
                - Chủ đề hiện tại: %s

                Nội dung chương trình liên quan:
                %s

                Lab ảo liên quan:
                %s

                Nguyên tắc:
                - Trả lời bằng tiếng Việt.
                - Giải thích dễ hiểu cho học sinh cấp 2.
                - Không dùng Markdown như **in đậm**, tiêu đề #, bảng hoặc danh sách dùng dấu *.
                - Không dùng LaTeX hoặc ký tự backslash. Viết công thức bằng văn bản thường, ví dụ n_Fe = m_Fe / M_Fe = 11,2 / 56 = 0,2 mol.
                - Với công thức hóa học, viết dạng H2O, CO2, FeCl2, H2; không dùng chỉ số LaTeX.
                - Không dùng HTML như <sub>, <sup>, <br>. Viết FeCl2, H2, n_Fe bằng văn bản thường.
                - Không bịa chương trình học.
                - Không dạy vượt quá chương trình nếu không cần thiết.
                - Nếu câu hỏi ngoài phạm vi, hãy nói nhẹ nhàng rằng phần này sẽ học ở lớp cao hơn.
                - Ưu tiên ví dụ an toàn, quen thuộc.
                - Nếu có lab ảo liên quan, hãy gợi ý ngắn gọn.

                Câu hỏi:
                %s
                """.formatted(
                request.getGrade(),
                request.getBookType(),
                cleanTopic(topic),
                curriculumContext,
                labContext,
                request.getMessage()
        );
    }

    private String buildImageChatPrompt(Integer grade,
                                        BookType bookType,
                                        String topic,
                                        String message,
                                        String curriculumContext,
                                        List<SuggestedLabDTO> labs) {
        String labContext = labs.isEmpty()
                ? "Không có lab ảo liên quan."
                : labs.stream().map(lab -> "- " + lab.getTitle()).collect(Collectors.joining("\n"));

        return """
                Bạn là ChemAI Tutor, trợ lý học tập môn KHTN/Hóa học THCS Việt Nam.

                Học sinh vừa gửi một ảnh chụp đề bài cần hỗ trợ.

                Thông tin học sinh:
                - Lớp: %s
                - Bộ sách: %s
                - Chủ đề dự đoán: %s

                Nội dung chương trình liên quan:
                %s

                Lab ảo liên quan:
                %s

                Yêu cầu xử lý ảnh:
                - Đọc nội dung đề trong ảnh trước khi giải.
                - Nếu ảnh mờ, thiếu dữ kiện hoặc không đọc rõ đề, hãy nói rõ phần chưa đọc được và yêu cầu học sinh chụp lại.
                - Nếu đọc được đề, hãy giải thích bằng tiếng Việt, ngắn gọn, dễ hiểu cho học sinh cấp 2.
                - Ưu tiên hướng dẫn từng bước: nhận dạng dữ kiện, kiến thức cần dùng, cách làm, kết luận.
                - Không bịa dữ kiện không có trong ảnh.
                - Không dạy vượt quá chương trình nếu không cần thiết; nếu ngoài phạm vi, hãy nói nhẹ nhàng.
                - Không dùng Markdown như **in đậm**, tiêu đề #, bảng hoặc danh sách dùng dấu *.
                - Không dùng LaTeX hoặc ký tự backslash.
                - Không dùng HTML như <sub>, <sup>, <br>. Viết FeCl2, H2, n_Fe bằng văn bản thường.
                - Với bài tính toán, ghi công thức và phép thế số rõ ràng, dùng công thức hóa học dạng văn bản như H2O, CO2, FeCl2.
                - Nếu có lab ảo phù hợp, gợi ý ngắn gọn ở cuối.

                Câu hỏi thêm của học sinh:
                %s
                """.formatted(
                grade,
                bookType,
                cleanTopic(topic),
                curriculumContext,
                labContext,
                message
        );
    }

    private String buildGenerateExamPrompt(GenerateExamRequest request, String curriculumContext) {
        return """
                Bạn là giáo viên KHTN/Hóa học THCS Việt Nam.

                Hãy tạo đề ôn tập theo thông tin:
                - Lớp: %s
                - Bộ sách: %s
                - Loại đề: %s
                - Chủ đề: %s
                - Độ khó: %s
                - Số câu: %s
                - Số câu tính toán tối thiểu: %s

                Nội dung chương trình được phép dùng:
                %s

                Yêu cầu:
                - Không dùng kiến thức ngoài chương trình.
                - Tất cả câu hỏi phải bám trực tiếp chủ đề "%s".
                - Nếu chủ đề là một chất/nguyên tố cụ thể như Sắt/Fe, mọi câu phải nhắc trực tiếp tới chất đó, hợp chất của nó, ứng dụng hoặc phản ứng của nó.
                - Không đưa câu hỏi chung chung lệch chủ đề, ví dụ đề Sắt thì không hỏi riêng về biến đổi vật lí/hóa học nếu không gắn với sắt.
                - Có câu tính toán định lượng phù hợp chương trình, ví dụ tính khối lượng, số mol, thể tích khí hoặc lượng chất theo phương trình hóa học nếu chủ đề cho phép.
                - Có trắc nghiệm, tự luận và câu vận dụng/lab nếu phù hợp.
                - Câu hỏi rõ ràng, phù hợp học sinh cấp 2.
                - Trả về duy nhất một JSON object hợp lệ, không thêm markdown, không thêm giải thích ngoài JSON.
                - Không dùng ký tự backslash hoặc LaTeX. Viết công thức hóa học dạng văn bản thường như H2O, CO2, NaCl.
                - Trường type chỉ được là MULTIPLE_CHOICE, ESSAY hoặc LAB_APPLICATION.
                - Với MULTIPLE_CHOICE: options phải có đúng 4 chuỗi, answer phải trùng chính xác một chuỗi trong options.
                - Với ESSAY hoặc LAB_APPLICATION: options là mảng rỗng [].

                JSON format:
                {
                  "title": "...",
                  "durationMinutes": 45,
                  "questions": [
                    {
                      "type": "MULTIPLE_CHOICE",
                      "question": "...",
                      "options": ["...", "...", "...", "..."],
                      "answer": "...",
                      "explanation": "...",
                      "topic": "..."
                    }
                  ],
                  "answerKey": [
                    {
                      "questionIndex": 1,
                      "answer": "...",
                      "explanation": "..."
                    }
                  ]
                }
                """.formatted(
                request.getGrade(),
                request.getBookType(),
                request.getExamType(),
                cleanTopic(request.getTopic()),
                request.getDifficulty(),
                questionCountForExamType(request.getExamType()),
                calculationQuestionCountForExamType(request.getExamType()),
                curriculumContext,
                cleanTopic(request.getTopic())
        );
    }

    private int questionCountForExamType(ExamType examType) {
        if (examType == ExamType.QUIZ_15_MIN) {
            return 5;
        }
        if (examType == ExamType.FORTY_FIVE_MINUTES) {
            return 8;
        }
        return 10;
    }

    private int calculationQuestionCountForExamType(ExamType examType) {
        if (examType == ExamType.QUIZ_15_MIN) {
            return 1;
        }
        return 2;
    }

    private GenerateExamResponse parseAndValidateExam(String rawJson) {
        try {
            GenerateExamResponse response = objectMapper.readValue(extractJsonObject(rawJson), GenerateExamResponse.class);
            if (isBlank(response.getTitle()) || response.getDurationMinutes() == null || response.getDurationMinutes() <= 0) {
                throw new CustomExceptions.BadRequestException("AI generated exam is missing title or duration");
            }
            if (response.getQuestions() == null || response.getQuestions().isEmpty()) {
                throw new CustomExceptions.BadRequestException("AI generated exam has no questions");
            }
            List<AnswerKeyDTO> normalizedAnswerKey = new ArrayList<>();
            for (int i = 0; i < response.getQuestions().size(); i++) {
                GeneratedQuestionDTO question = response.getQuestions().get(i);
                if (question.getType() == null || isBlank(question.getQuestion()) || isBlank(question.getAnswer())) {
                    throw new CustomExceptions.BadRequestException("AI generated exam contains an invalid question");
                }
                if (question.getType() == AiQuestionType.MULTIPLE_CHOICE
                        && (question.getOptions() == null || question.getOptions().size() != 4)) {
                    throw new CustomExceptions.BadRequestException("Multiple-choice questions must have 4 options");
                }
                if (question.getType() == AiQuestionType.MULTIPLE_CHOICE
                        && question.getOptions().stream().noneMatch(option -> normalizeAnswer(option).equals(normalizeAnswer(question.getAnswer())))) {
                    throw new CustomExceptions.BadRequestException("Multiple-choice answer must match one option");
                }
                if (question.getType() != AiQuestionType.MULTIPLE_CHOICE && question.getOptions() == null) {
                    question.setOptions(List.of());
                }
                normalizedAnswerKey.add(AnswerKeyDTO.builder()
                        .questionIndex(i + 1)
                        .answer(question.getAnswer())
                        .explanation(blankToNull(question.getExplanation()) == null ? "" : question.getExplanation())
                        .build());
            }
            if (response.getAnswerKey() == null || response.getAnswerKey().isEmpty()) {
                response.setAnswerKey(normalizedAnswerKey);
            }
            return response;
        } catch (JsonProcessingException ex) {
            throw new CustomExceptions.BadRequestException("AI generated invalid JSON: " + ex.getOriginalMessage());
        }
    }

    private String extractJsonObject(String value) {
        if (value == null) {
            throw new CustomExceptions.BadRequestException("AI generated invalid JSON");
        }

        String trimmed = value.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```[a-zA-Z]*\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }

        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace < 0 || lastBrace <= firstBrace) {
            throw new CustomExceptions.BadRequestException("AI generated invalid JSON");
        }

        return trimmed.substring(firstBrace, lastBrace + 1);
    }

    private String sanitizeAiAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        String cleaned = answer
                .replaceAll("(?is)<sub[^>]*>(.*?)</sub>", "_$1")
                .replaceAll("(?is)<sup[^>]*>(.*?)</sup>", "^$1")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?is)<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replaceAll("\\\\frac\\{([^{}]+)}\\{([^{}]+)}", "($1)/($2)")
                .replaceAll("\\\\text\\{([^{}]+)}", "$1")
                .replace("\\left", "")
                .replace("\\right", "")
                .replace("\\times", " x ")
                .replace("\\cdot", " . ")
                .replace("\\_", "_")
                .replace("$", "")
                .replaceAll("\\*\\*([^*]+)\\*\\*", "$1")
                .replaceAll("__([^_]+)__", "$1")
                .replaceAll("(?m)^\\s{0,3}#{1,6}\\s+", "")
                .replaceAll("(?m)^\\s{0,3}[-*]\\s+", "- ")
                .replaceAll("\\\\([a-zA-Z]+)", "$1")
                .replaceAll("[ \\t]+\\n", "\n");
        return normalizeChemText(cleaned).trim();
    }

    private String normalizeChemText(String value) {
        return value
                .replace('₀', '0')
                .replace('₁', '1')
                .replace('₂', '2')
                .replace('₃', '3')
                .replace('₄', '4')
                .replace('₅', '5')
                .replace('₆', '6')
                .replace('₇', '7')
                .replace('₈', '8')
                .replace('₉', '9')
                .replace("â‚€", "0")
                .replace("â‚", "1")
                .replace("â‚‚", "2")
                .replace("â‚ƒ", "3")
                .replace("â‚„", "4")
                .replace("â‚…", "5")
                .replace("â‚†", "6")
                .replace("â‚‡", "7")
                .replace("â‚ˆ", "8")
                .replace("â‚‰", "9")
                .replace("â†’", "->")
                .replace("→", "->")
                .replace("â€¢", "-")
                .replace("•", "-");
    }

    private boolean isAutoGradable(GeneratedQuestionDTO question) {
        return question.getType() == AiQuestionType.MULTIPLE_CHOICE
                || question.getType() == AiQuestionType.ESSAY
                || question.getType() == AiQuestionType.LAB_APPLICATION;
    }

    private boolean gradeAnswer(GeneratedQuestionDTO question, String studentAnswer) {
        if (isBlank(studentAnswer)) {
            return false;
        }
        if (question.getType() == AiQuestionType.MULTIPLE_CHOICE) {
            return normalizeAnswer(studentAnswer).equals(normalizeAnswer(question.getAnswer()));
        }
        return isMeaningfullySimilar(studentAnswer, question.getAnswer() + " " + nullToBlank(question.getExplanation()));
    }

    private boolean isMeaningfullySimilar(String studentAnswer, String expectedAnswer) {
        Set<String> studentTokens = meaningfulTokens(studentAnswer);
        Set<String> expectedTokens = meaningfulTokens(expectedAnswer);
        if (studentTokens.isEmpty() || expectedTokens.isEmpty()) {
            return false;
        }

        long matchedTokens = expectedTokens.stream().filter(studentTokens::contains).count();
        double tokenRatio = matchedTokens / (double) expectedTokens.size();

        Set<String> studentFormulas = formulaTokens(studentAnswer);
        Set<String> expectedFormulas = formulaTokens(expectedAnswer);
        long matchedFormulas = expectedFormulas.stream().filter(studentFormulas::contains).count();
        double formulaRatio = expectedFormulas.isEmpty() ? 0 : matchedFormulas / (double) expectedFormulas.size();

        return tokenRatio >= 0.35
                || matchedTokens >= 4
                || (matchedFormulas > 0 && matchedTokens >= 2)
                || formulaRatio >= 0.5;
    }

    private Set<String> meaningfulTokens(String value) {
        Set<String> stopWords = Set.of(
                "la", "va", "co", "cac", "mot", "nhung", "duoc", "trong", "voi", "cua", "cho", "vao",
                "sau", "truoc", "khi", "nen", "do", "day", "nay", "thi", "tao", "ra", "dung", "dich",
                "chat", "hoa", "hoc", "phan", "ung", "hien", "tuong", "phuong", "trinh"
        );
        String normalized = normalizeAnswer(value);
        if (normalized.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(normalized.split("\\s+"))
                .filter(token -> token.length() > 2)
                .filter(token -> !stopWords.contains(token))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> formulaTokens(String value) {
        String normalized = normalizeAnswer(value);
        if (normalized.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(normalized.split("\\s+"))
                .filter(token -> token.matches(".*\\d.*") || token.matches("[a-z]{1,3}"))
                .filter(token -> token.length() <= 8)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private String normalizeAnswer(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return normalized.replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim().replaceAll("\\s+", " ");
    }

    private MasteryLevel calculateMasteryLevel(double accuracy) {
        if (accuracy < 0.5) {
            return MasteryLevel.WEAK;
        }
        if (accuracy <= 0.8) {
            return MasteryLevel.MEDIUM;
        }
        return MasteryLevel.GOOD;
    }

    private List<String> buildStrengths(MasteryLevel masteryLevel, String topic, double accuracy) {
        int percent = (int) Math.round(accuracy * 100);
        if (masteryLevel == MasteryLevel.GOOD) {
            return List.of("Bạn đang nắm chắc chủ đề " + topic + " với độ chính xác khoảng " + percent + "%.");
        }
        if (masteryLevel == MasteryLevel.MEDIUM) {
            return List.of("Bạn đã có nền tảng ban đầu về " + topic + ", nhưng cần luyện thêm để chắc hơn.");
        }
        return List.of("Bạn đã hoàn thành bài làm, đây là dữ liệu tốt để chọn đúng phần cần ôn lại.");
    }

    private List<String> buildWeaknesses(MasteryLevel masteryLevel, String topic, List<String> wrongTopics) {
        if (!wrongTopics.isEmpty()) {
            return wrongTopics.stream()
                    .map(wrongTopic -> "Cần ôn lại: " + wrongTopic)
                    .toList();
        }
        if (masteryLevel == MasteryLevel.WEAK) {
            return List.of("Cần học lại khái niệm cốt lõi của chủ đề " + topic + ".");
        }
        if (masteryLevel == MasteryLevel.MEDIUM) {
            return List.of("Cần luyện thêm câu vận dụng và giải thích hiện tượng.");
        }
        return List.of("Chưa ghi nhận điểm yếu rõ ràng trong lần làm này.");
    }

    private void addLab(Map<UUID, SuggestedLabDTO> labs, Lab lab) {
        if (lab == null || lab.getId() == null || labs.containsKey(lab.getId())) {
            return;
        }
        labs.put(lab.getId(), SuggestedLabDTO.builder()
                .id(lab.getId())
                .title(lab.getTitle())
                .description(lab.getDescription())
                .category(lab.getCategory() == null ? null : lab.getCategory().name())
                .difficulty(lab.getDifficulty() == null ? null : lab.getDifficulty().name())
                .build());
    }

    private RecommendedLessonDTO toRecommendedLesson(Lesson lesson) {
        return RecommendedLessonDTO.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .chapterTitle(lesson.getChapter() == null ? null : lesson.getChapter().getTitle())
                .durationMinutes(lesson.getDurationMinutes())
                .topic(lesson.getTitle())
                .build();
    }

    private AiChatSessionResponse toSessionResponse(AiChatSession session) {
        return AiChatSessionResponse.builder()
                .id(session.getId())
                .grade(session.getGrade())
                .bookType(session.getBookType())
                .topic(session.getTopic())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    private GeneratedExamSummaryResponse toGeneratedExamSummary(AiGeneratedExam exam) {
        return GeneratedExamSummaryResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .grade(exam.getGrade())
                .bookType(exam.getBookType())
                .examType(exam.getExamType())
                .topic(exam.getTopic())
                .difficulty(exam.getDifficulty())
                .durationMinutes(exam.getDurationMinutes())
                .questionCount(exam.getQuestions() == null ? 0 : exam.getQuestions().size())
                .createdAt(exam.getCreatedAt())
                .build();
    }

    private GenerateExamResponse toGenerateExamResponse(AiGeneratedExam exam) {
        return GenerateExamResponse.builder()
                .examId(exam.getId())
                .title(exam.getTitle())
                .durationMinutes(exam.getDurationMinutes())
                .questions(objectMapper.convertValue(exam.getQuestions(), new TypeReference<List<GeneratedQuestionDTO>>() {}))
                .answerKey(objectMapper.convertValue(exam.getAnswerKey(), new TypeReference<List<AnswerKeyDTO>>() {}))
                .build();
    }

    private AiChatMessageResponse toMessageResponse(AiChatMessage message) {
        return AiChatMessageResponse.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getRole() == AiMessageRole.ASSISTANT ? sanitizeAiAnswer(message.getContent()) : message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private List<Map<String, Object>> toCacheLabs(List<SuggestedLabDTO> labs) {
        return objectMapper.convertValue(labs == null ? List.of() : labs, new TypeReference<>() {});
    }

    private List<SuggestedLabDTO> toSuggestedLabs(List<Map<String, Object>> cachedLabs) {
        if (cachedLabs == null) {
            return List.of();
        }
        return cachedLabs.stream()
                .map(map -> objectMapper.convertValue(map, SuggestedLabDTO.class))
                .toList();
    }

    private List<String> sanitizeTopics(List<String> topics) {
        if (topics == null) {
            return List.of();
        }
        return topics.stream()
                .map(this::cleanTopic)
                .filter(topic -> !topic.isBlank())
                .distinct()
                .toList();
    }

    private String cleanTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            return "Chủ đề chung";
        }
        return topic.trim().replaceAll("\\s+", " ");
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeForCache(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return normalized.replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim().replaceAll("\\s+", " ");
    }

    private String buildCacheKey(Integer grade, BookType bookType, String topic, String normalizedQuestion) {
        String raw = grade + "|" + bookType + "|" + normalizeForCache(cleanTopic(topic)) + "|" + normalizedQuestion;
        return sha256Hex(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        return html
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "" : value;
        }
        return value.substring(0, maxLength) + "...";
    }
}
