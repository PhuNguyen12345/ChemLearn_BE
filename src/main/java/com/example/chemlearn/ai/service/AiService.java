package com.example.chemlearn.ai.service;

import com.example.chemlearn.ai.config.AiProperties;
import com.example.chemlearn.ai.dto.*;
import com.example.chemlearn.ai.entity.*;
import com.example.chemlearn.ai.enums.*;
import com.example.chemlearn.ai.provider.AiProviderClient;
import com.example.chemlearn.ai.repository.*;
import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.lab.entity.Lab;
import com.example.chemlearn.lab.enums.LabType;
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
    private static final String TTS_EXPLANATION_DELIMITER = "===TTS_EXPLANATION===";
    private static final String CHAT_CACHE_VERSION = "chat-solution-only-tts-trends-v3";
    private static final int MAX_VISIBLE_CHAT_LINES = 80;
    private static final int MAX_VISIBLE_CHAT_CHARS = 6000;

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
                ChatAnswerParts cachedParts = toChatAnswerParts(cache.getAnswer());
                saveMessage(session, AiMessageRole.ASSISTANT, cachedParts.toStoredContent());
                touchSession(session);
                return AiChatResponse.builder()
                        .sessionId(session.getId())
                        .topic(chatTopic)
                        .answer(cachedParts.answer())
                        .speechText(cachedParts.speechText())
                        .suggestedLabs(cachedLabs)
                        .build();
            }
        }

        String curriculumContext = buildCurriculumContext(request.getGrade(), request.getBookType(), chatTopic);
        List<SuggestedLabDTO> suggestedLabs = findSuggestedLabs(request.getGrade(), request.getBookType(), chatTopic);
        String prompt = buildChatPrompt(request, chatTopic, curriculumContext, suggestedLabs);
        boolean fallbackUsed = false;
        String rawAnswer;
        try {
            rawAnswer = aiProviderClient.chat(prompt);
        } catch (RuntimeException ex) {
            if (!shouldUseAiFallback(ex)) {
                throw ex;
            }
            fallbackUsed = true;
            rawAnswer = buildChatFallbackAnswer(chatTopic, request.getMessage(), curriculumContext);
        }
        ChatAnswerParts answerParts = toChatAnswerParts(rawAnswer);

        saveMessage(session, AiMessageRole.ASSISTANT, answerParts.toStoredContent());
        touchSession(session);

        if (aiProperties.isCacheEnabled() && !fallbackUsed) {
            AiResponseCache cache = new AiResponseCache();
            cache.setCacheKey(cacheKey);
            cache.setNormalizedQuestion(normalizedQuestion);
            cache.setGrade(request.getGrade());
            cache.setBookType(request.getBookType());
            cache.setTopic(chatTopic);
            cache.setAnswer(answerParts.toStoredContent());
            cache.setSuggestedLabs(toCacheLabs(suggestedLabs));
            aiResponseCacheRepository.save(cache);
        }

        return AiChatResponse.builder()
                .sessionId(session.getId())
                .topic(chatTopic)
                .answer(answerParts.answer())
                .speechText(answerParts.speechText())
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
                ChatAnswerParts cachedParts = toChatAnswerParts(cache.getAnswer());
                saveMessage(session, AiMessageRole.ASSISTANT, cachedParts.toStoredContent());
                touchSession(session);
                return AiChatResponse.builder()
                        .sessionId(session.getId())
                        .topic(chatTopic)
                        .answer(cachedParts.answer())
                        .speechText(cachedParts.speechText())
                        .suggestedLabs(cachedLabs)
                        .build();
            }
        }

        String curriculumContext = buildCurriculumContext(grade, bookType, chatTopic);
        List<SuggestedLabDTO> suggestedLabs = findSuggestedLabs(grade, bookType, chatTopic);
        String prompt = buildImageChatPrompt(grade, bookType, chatTopic, userPrompt, curriculumContext, suggestedLabs);
        boolean fallbackUsed = false;
        String rawAnswer;
        try {
            rawAnswer = aiProviderClient.chatWithImage(prompt, mimeType, imageBytes);
        } catch (RuntimeException ex) {
            if (!shouldUseAiFallback(ex)) {
                throw ex;
            }
            fallbackUsed = true;
            rawAnswer = buildImageChatFallbackAnswer(chatTopic, userPrompt);
        }
        ChatAnswerParts answerParts = toChatAnswerParts(rawAnswer);

        saveMessage(session, AiMessageRole.ASSISTANT, answerParts.toStoredContent());
        touchSession(session);

        if (aiProperties.isCacheEnabled() && !fallbackUsed) {
            AiResponseCache cache = new AiResponseCache();
            cache.setCacheKey(cacheKey);
            cache.setNormalizedQuestion(normalizedQuestion);
            cache.setGrade(grade);
            cache.setBookType(bookType);
            cache.setTopic(chatTopic);
            cache.setAnswer(answerParts.toStoredContent());
            cache.setSuggestedLabs(toCacheLabs(suggestedLabs));
            aiResponseCacheRepository.save(cache);
        }

        return AiChatResponse.builder()
                .sessionId(session.getId())
                .topic(chatTopic)
                .answer(answerParts.answer())
                .speechText(answerParts.speechText())
                .suggestedLabs(suggestedLabs)
                .build();
    }

    @Transactional
    public GenerateExamResponse generateExam(GenerateExamRequest request) {
        Student student = getAuthorizedStudent(request.getStudentId());
        String curriculumContext = buildCurriculumContext(request.getGrade(), request.getBookType(), request.getTopic());
        String prompt = buildGenerateExamPrompt(request, curriculumContext);
        GenerateExamResponse response;
        try {
            response = parseAndValidateExam(aiProviderClient.generateExam(prompt));
        } catch (RuntimeException ex) {
            if (!shouldUseAiFallback(ex)) {
                throw ex;
            }
            response = buildFallbackExam(request);
        }

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

    public byte[] synthesizeSpeech(AiTtsRequest request) {
        String text = blankToNull(request.getText());
        if (text == null) {
            throw new CustomExceptions.BadRequestException("Text is required");
        }
        int maxChars = aiProperties.getTts().getMaxChars() == null ? 5000 : aiProperties.getTts().getMaxChars();
        if (text.length() > maxChars) {
            text = text.substring(0, maxChars).trim();
        }
        return aiProviderClient.synthesizeSpeech(text);
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
            labRepository.searchPremadeLabsByKeyword(LabType.PREMADE, keyword, PageRequest.of(0, MAX_RECOMMENDATIONS))
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

    private boolean shouldUseAiFallback(RuntimeException ex) {
        if (!aiProperties.isFallbackEnabled()) {
            return false;
        }

        String message = exceptionText(ex);
        if (message.contains("missing gemini_api_key")
                || message.contains("missing ai_api_key")
                || message.contains("missing ai.model")
                || message.contains("unauthorized")
                || message.contains("forbidden")
                || message.contains("invalid api key")
                || message.contains("blocked the prompt")
                || message.contains("image is required")
                || message.contains("only jpeg")
                || message.contains("not configured")) {
            return false;
        }

        return message.contains("429")
                || message.contains("quota")
                || message.contains("rate")
                || message.contains("resource_exhausted")
                || message.contains("too many requests")
                || message.contains("limit")
                || message.contains("exceeded")
                || message.contains("500")
                || message.contains("502")
                || message.contains("503")
                || message.contains("504")
                || message.contains("timeout")
                || message.contains("timed out")
                || message.contains("unavailable")
                || message.contains("overloaded")
                || message.contains("cannot call gemini")
                || message.contains("request was interrupted")
                || message.contains("empty response")
                || message.contains("ai generated invalid json")
                || message.contains("ai generated exam")
                || message.contains("multiple-choice");
    }

    private String exceptionText(Throwable throwable) {
        List<String> messages = new ArrayList<>();
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null) {
                messages.add(current.getMessage());
            }
            current = current.getCause();
        }
        return String.join(" ", messages).toLowerCase(Locale.ROOT);
    }

    private String buildChatFallbackAnswer(String topic, String message, String curriculumContext) {
        String contextHint = truncate(stripHtml(curriculumContext), 280);
        return sanitizeAiAnswer("""
                ChemAI đang ở chế độ dự phòng vì dịch vụ AI đang quá tải hoặc hết lượt request tạm thời.

                Mình vẫn có thể giúp em theo cách cơ bản:
                - Chủ đề: %s
                - Câu hỏi của em: %s
                - Trước hết, hãy gạch chân dữ kiện chính trong đề.
                - Nếu là câu hỏi lý thuyết, hãy xác định khái niệm, hiện tượng, dấu hiệu nhận biết và ví dụ an toàn.
                - Nếu là bài tính toán, hãy viết dữ kiện, đổi về mol nếu cần, lập phương trình hóa học đã cân bằng, rồi tính theo tỉ lệ mol.
                - Nếu có phản ứng hóa học, nhớ kiểm tra chất tạo thành và cân bằng số nguyên tử hai vế.

                Gợi ý từ nội dung bài học đang có:
                %s

                Em có thể gửi lại câu hỏi ngắn hơn hoặc thử lại sau vài phút để ChemAI giải chi tiết bằng AI.
                """.formatted(cleanTopic(topic), blankToNull(message) == null ? "Chưa có nội dung cụ thể." : message.trim(), contextHint));
    }

    private String buildImageChatFallbackAnswer(String topic, String message) {
        return sanitizeAiAnswer("""
                ChemAI đang ở chế độ dự phòng vì dịch vụ đọc ảnh bằng AI đang quá tải hoặc hết lượt request tạm thời.

                Mình chưa thể đọc nội dung trong ảnh ở thời điểm này. Em có thể:
                - Gõ lại nội dung đề hoặc phần dữ kiện chính.
                - Chụp lại ảnh rõ nét hơn và thử gửi lại sau vài phút.
                - Nếu là bài tính toán hóa học, hãy nhập các số liệu như khối lượng, thể tích khí, chất tham gia và yêu cầu cần tính.

                Chủ đề dự đoán: %s
                Câu hỏi thêm của em: %s
                """.formatted(cleanTopic(topic), blankToNull(message) == null ? "Không có." : message.trim()));
    }

    private GenerateExamResponse buildFallbackExam(GenerateExamRequest request) {
        String topic = cleanTopic(request.getTopic());
        List<GeneratedQuestionDTO> questions = new ArrayList<>();
        questions.add(fallbackMultipleChoice(
                "Khi ôn tập chủ đề " + topic + ", bước nào nên làm đầu tiên?",
                List.of("Ghi lại dữ kiện và khái niệm chính", "Đoán đáp án ngay", "Bỏ qua hiện tượng", "Chỉ học thuộc đáp án"),
                "Ghi lại dữ kiện và khái niệm chính",
                "Xác định dữ kiện và khái niệm giúp tránh nhầm lẫn khi giải bài.",
                topic
        ));
        questions.add(fallbackMultipleChoice(
                "Một mẫu chất có khối lượng 11,2 g và khối lượng mol là 56 g/mol. Số mol của mẫu chất là bao nhiêu?",
                List.of("0,1 mol", "0,2 mol", "0,5 mol", "2 mol"),
                "0,2 mol",
                "Dùng công thức n = m / M = 11,2 / 56 = 0,2 mol.",
                "Tính toán hóa học"
        ));
        questions.add(fallbackMultipleChoice(
                "Khi viết phương trình hóa học, yêu cầu nào là quan trọng nhất?",
                List.of("Hai vế có cùng số nguyên tử mỗi nguyên tố", "Chỉ cần viết chất tham gia", "Không cần sản phẩm", "Đổi tùy ý công thức hóa học"),
                "Hai vế có cùng số nguyên tử mỗi nguyên tố",
                "Phương trình hóa học đúng phải bảo toàn số nguyên tử của từng nguyên tố.",
                topic
        ));
        questions.add(fallbackEssay(
                "Trình bày ngắn gọn các ý chính cần nhớ về chủ đề " + topic + ".",
                "Nêu khái niệm chính, dấu hiệu nhận biết hoặc công thức cần dùng, sau đó đưa một ví dụ phù hợp với bài học.",
                "Câu trả lời cần có khái niệm, dấu hiệu/công thức và ví dụ.",
                topic
        ));
        questions.add(fallbackLabApplication(
                "Nếu gặp một thí nghiệm liên quan đến " + topic + ", em cần ghi lại những quan sát nào?",
                "Ghi chất ban đầu, hiện tượng quan sát được, màu sắc, khí/kết tủa nếu có, điều kiện thí nghiệm và kết luận.",
                "Quan sát đầy đủ giúp giải thích hiện tượng chính xác hơn.",
                topic
        ));
        questions.add(fallbackMultipleChoice(
                "Ở điều kiện tiêu chuẩn, 0,25 mol khí có thể tích bao nhiêu?",
                List.of("5,60 lít", "6,20 lít", "11,20 lít", "24,79 lít"),
                "6,20 lít",
                "Theo chương trình mới, ở điều kiện chuẩn 25°C và 1 bar, Vm = 24,79 L/mol nên V = 0,25 x 24,79 = 6,1975 ≈ 6,20 lít.",
                "Tính toán hóa học"
        ));
        questions.add(fallbackEssay(
                "Vì sao khi giải bài tập hóa học cần cân bằng phương trình trước khi tính toán?",
                "Vì phương trình đã cân bằng cho biết đúng tỉ lệ mol giữa các chất tham gia và sản phẩm.",
                "Tỉ lệ mol chỉ dùng được khi phương trình đã cân bằng.",
                topic
        ));
        questions.add(fallbackMultipleChoice(
                "Dấu hiệu nào thường cho thấy có phản ứng hóa học xảy ra?",
                List.of("Có chất khí, kết tủa, đổi màu hoặc tỏa nhiệt", "Chỉ thay đổi hình dạng", "Chỉ thay đổi kích thước", "Chỉ thay đổi vị trí"),
                "Có chất khí, kết tủa, đổi màu hoặc tỏa nhiệt",
                "Phản ứng hóa học tạo chất mới, thường có các dấu hiệu như khí, kết tủa, đổi màu hoặc tỏa nhiệt.",
                topic
        ));
        questions.add(fallbackMultipleChoice(
                "Nếu m = 8 g và M = 40 g/mol, số mol bằng bao nhiêu?",
                List.of("0,1 mol", "0,2 mol", "0,4 mol", "5 mol"),
                "0,2 mol",
                "n = m / M = 8 / 40 = 0,2 mol.",
                "Tính toán hóa học"
        ));
        questions.add(fallbackEssay(
                "Nêu một lỗi thường gặp khi làm bài về " + topic + " và cách tránh lỗi đó.",
                "Một lỗi thường gặp là bỏ sót dữ kiện hoặc dùng phương trình chưa cân bằng. Cách tránh là đọc kỹ đề, ghi dữ kiện và kiểm tra phương trình trước khi tính.",
                "Câu trả lời cần chỉ ra lỗi và cách phòng tránh.",
                topic
        ));
        questions.add(fallbackMultipleChoice(
                "Ở điều kiện chuẩn 25°C và 1 bar, 0,10 mol khí H2 có thể tích xấp xỉ bao nhiêu?",
                List.of("2,24 lít", "2,48 lít", "22,4 lít", "24,79 lít"),
                "2,48 lít",
                "V = n x Vm = 0,10 x 24,79 = 2,479 lít ≈ 2,48 lít.",
                "Tính toán hóa học"
        ));
        questions.add(fallbackMultipleChoice(
                "Khi đề bài có cả chất tham gia A và B, bước nào giúp tránh tính sai lượng sản phẩm?",
                List.of("So sánh số mol theo tỉ lệ phương trình để tìm chất hết", "Luôn lấy chất có khối lượng nhỏ hơn", "Bỏ qua hệ số phương trình", "Chỉ dùng đáp án trắc nghiệm để suy ra"),
                "So sánh số mol theo tỉ lệ phương trình để tìm chất hết",
                "Bài có nhiều chất tham gia thường cần xác định chất hết/chất dư trước khi tính sản phẩm.",
                topic
        ));
        questions.add(fallbackMultipleChoice(
                "Một chất có n = 0,15 mol và M = 56 g/mol. Khối lượng của chất đó là bao nhiêu?",
                List.of("5,6 g", "8,4 g", "11,2 g", "37,3 g"),
                "8,4 g",
                "m = n x M = 0,15 x 56 = 8,4 g.",
                "Tính toán hóa học"
        ));
        questions.add(fallbackEssay(
                "Lập dàn ý giải một bài toán định lượng thuộc chủ đề " + topic + " có dữ kiện khối lượng và thể tích khí ở điều kiện chuẩn.",
                "Cần ghi dữ kiện, đổi khối lượng hoặc thể tích khí về số mol, viết và cân bằng phương trình hóa học, dùng tỉ lệ mol để tính đại lượng cần tìm, rồi kết luận có đơn vị.",
                "Với thể tích khí ở điều kiện chuẩn theo chương trình mới, dùng Vm = 24,79 L/mol nếu đề không nêu quy ước khác.",
                topic
        ));
        questions.add(fallbackLabApplication(
                "Trong một thí nghiệm thuộc chủ đề " + topic + ", nếu thấy khí thoát ra ít hơn dự đoán, hãy nêu hai nguyên nhân có thể xảy ra.",
                "Có thể do chất phản ứng chưa đủ, phản ứng chưa hoàn toàn, khí bị thất thoát khi thu hoặc điều kiện thí nghiệm chưa phù hợp.",
                "Câu vận dụng cần liên hệ hiện tượng quan sát với lượng chất và điều kiện phản ứng.",
                topic
        ));

        int count = Math.min(questionCountForExamType(request.getExamType()), questions.size());
        List<GeneratedQuestionDTO> selectedQuestions = orderExamQuestions(questions.subList(0, count));
        List<AnswerKeyDTO> answerKey = new ArrayList<>();
        for (int i = 0; i < selectedQuestions.size(); i++) {
            GeneratedQuestionDTO question = selectedQuestions.get(i);
            answerKey.add(AnswerKeyDTO.builder()
                    .questionIndex(i + 1)
                    .answer(question.getAnswer())
                    .explanation(question.getExplanation())
                    .build());
        }

        return GenerateExamResponse.builder()
                .title("Đề ôn tập dự phòng - " + topic)
                .durationMinutes(durationMinutesForExamType(request.getExamType()))
                .questions(selectedQuestions)
                .answerKey(answerKey)
                .build();
    }

    private GeneratedQuestionDTO fallbackMultipleChoice(String question, List<String> options, String answer, String explanation, String topic) {
        return GeneratedQuestionDTO.builder()
                .type(AiQuestionType.MULTIPLE_CHOICE)
                .question(question)
                .options(options)
                .answer(answer)
                .explanation(explanation)
                .topic(topic)
                .build();
    }

    private GeneratedQuestionDTO fallbackEssay(String question, String answer, String explanation, String topic) {
        return GeneratedQuestionDTO.builder()
                .type(AiQuestionType.ESSAY)
                .question(question)
                .options(List.of())
                .answer(answer)
                .explanation(explanation)
                .topic(topic)
                .build();
    }

    private GeneratedQuestionDTO fallbackLabApplication(String question, String answer, String explanation, String topic) {
        return GeneratedQuestionDTO.builder()
                .type(AiQuestionType.LAB_APPLICATION)
                .question(question)
                .options(List.of())
                .answer(answer)
                .explanation(explanation)
                .topic(topic)
                .build();
    }

    private int durationMinutesForExamType(ExamType examType) {
        if (examType == ExamType.QUIZ_15_MIN) {
            return 15;
        }
        if (examType == ExamType.MIDTERM || examType == ExamType.FINAL) {
            return 60;
        }
        return 45;
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
                - Dùng Markdown giống ChatGPT: đoạn văn ngắn, danh sách đánh số, chữ đậm cho ý chính.
                - Dùng LaTeX cho công thức và phương trình, đặt công thức quan trọng trong $$...$$.
                - Với phân số phải dùng \\frac{...}{...}; với mũi tên phản ứng dùng \\rightarrow; với chỉ số dùng _{...}, ví dụ H_2, FeCl_2.
                - Không dùng HTML như <sub>, <sup>, <br>.
                - Theo chương trình mới, nếu đề ghi "điều kiện chuẩn", "đkc" hoặc "đktc" mà không nêu nhiệt độ/áp suất khác, dùng V_m = 24,79 L/mol ở 25°C và 1 bar.
                - Chỉ dùng 22,4 L/mol khi đề ghi rõ 0°C, 1 atm hoặc nói theo quy ước cũ.
                - Không bịa chương trình học.
                - Không dạy vượt quá chương trình nếu không cần thiết.
                - Nếu câu hỏi ngoài phạm vi, hãy nói nhẹ nhàng rằng phần này sẽ học ở lớp cao hơn.
                - Ưu tiên ví dụ an toàn, quen thuộc.
                - Nếu có lab ảo liên quan, hãy gợi ý ngắn gọn.

                Bắt buộc định dạng câu trả lời:
                - Phần hiển thị trước dòng "%s" chỉ là bài giải, không giảng giải lan man, không chào hỏi.
                - Trình bày giống ChatGPT nhưng gọn: mỗi ý một dòng hoặc một đoạn ngắn, có thụt đầu dòng bằng danh sách đánh số.
                - Với bài tính toán, phải có các mục rõ ràng: a) Phương trình, b) Tính số mol, c) Suy ra chất/đáp án.
                - Mọi phương trình và phép toán quan trọng đặt riêng một dòng bằng $$...$$ để frontend căn giữa.
                - Không nhét công thức vào giữa đoạn văn dài. Sau mỗi công thức nên xuống dòng.
                - Phần hiển thị không dùng câu trend, không giải thích vì sao quá dài; chỉ ghi cách làm và kết quả.
                - Sau phần hiển thị, viết đúng một dòng riêng: %s
                - Sau dòng đó là phần giải thích kỹ hơn để hệ thống đọc bằng giọng nói. Phần này viết bằng văn nói tiếng Việt tự nhiên, không dùng LaTeX, không dùng Markdown.
                - Phần đọc được phép dùng nhiều câu trend thân thiện với học sinh cấp 2: "tính ra được ... là ngon luôn", "tin chuẩn em nhé", "mời đoàn mình di chuyển đến phần...", "thế mà lại hay", "vượt mức pickleball", "chốt đơn kiến thức", "đỉnh nóc kịch trần", "không lòng vòng".
                - Dùng câu trend đúng ngữ cảnh, mỗi đoạn 1 cụm là vừa; tuyệt đối không để phần hiển thị bị lố.
                - Không thêm tiêu đề JSON, không lặp lại những ý không cần thiết.

                Câu hỏi:
                %s
                """.formatted(
                request.getGrade(),
                request.getBookType(),
                cleanTopic(topic),
                curriculumContext,
                labContext,
                TTS_EXPLANATION_DELIMITER,
                TTS_EXPLANATION_DELIMITER,
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
                - Dùng Markdown giống ChatGPT: đoạn văn ngắn, danh sách đánh số, chữ đậm cho ý chính.
                - Dùng LaTeX cho công thức và phương trình, đặt công thức quan trọng trong $$...$$.
                - Với phân số phải dùng \\frac{...}{...}; với mũi tên phản ứng dùng \\rightarrow; với chỉ số dùng _{...}, ví dụ H_2, FeCl_2.
                - Không dùng HTML như <sub>, <sup>, <br>.
                - Theo chương trình mới, nếu đề ghi "điều kiện chuẩn", "đkc" hoặc "đktc" mà không nêu nhiệt độ/áp suất khác, dùng V_m = 24,79 L/mol ở 25°C và 1 bar.
                - Chỉ dùng 22,4 L/mol khi đề ghi rõ 0°C, 1 atm hoặc nói theo quy ước cũ.
                - Với bài tính toán, ghi công thức và phép thế số rõ ràng.
                - Nếu có lab ảo phù hợp, gợi ý ngắn gọn ở cuối.

                Bắt buộc định dạng câu trả lời:
                - Phần hiển thị trước dòng "%s" chỉ là bài giải, không giảng giải lan man, không chào hỏi.
                - Trình bày giống ChatGPT nhưng gọn: mỗi ý một dòng hoặc một đoạn ngắn, có thụt đầu dòng bằng danh sách đánh số.
                - Với bài tính toán, phải có các mục rõ ràng: a) Phương trình, b) Tính số mol, c) Suy ra chất/đáp án.
                - Mọi phương trình và phép toán quan trọng đặt riêng một dòng bằng $$...$$ để frontend căn giữa.
                - Không nhét công thức vào giữa đoạn văn dài. Sau mỗi công thức nên xuống dòng.
                - Phần hiển thị không dùng câu trend, không giải thích vì sao quá dài; chỉ ghi cách làm và kết quả.
                - Sau phần hiển thị, viết đúng một dòng riêng: %s
                - Sau dòng đó là phần giải thích kỹ hơn để hệ thống đọc bằng giọng nói. Phần này viết bằng văn nói tiếng Việt tự nhiên, không dùng LaTeX, không dùng Markdown.
                - Phần đọc được phép dùng nhiều câu trend thân thiện với học sinh cấp 2: "tính ra được ... là ngon luôn", "tin chuẩn em nhé", "mời đoàn mình di chuyển đến phần...", "thế mà lại hay", "vượt mức pickleball", "chốt đơn kiến thức", "đỉnh nóc kịch trần", "không lòng vòng".
                - Dùng câu trend đúng ngữ cảnh, mỗi đoạn 1 cụm là vừa; tuyệt đối không để phần hiển thị bị lố.
                - Không thêm tiêu đề JSON, không lặp lại những ý không cần thiết.

                Câu hỏi thêm của học sinh:
                %s
                """.formatted(
                grade,
                bookType,
                cleanTopic(topic),
                curriculumContext,
                labContext,
                TTS_EXPLANATION_DELIMITER,
                TTS_EXPLANATION_DELIMITER,
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

                Chuẩn độ khó bắt buộc:
                %s

                Nội dung chương trình được phép dùng:
                %s

                Yêu cầu:
                - Không dùng kiến thức ngoài chương trình.
                - Tất cả câu hỏi phải bám trực tiếp chủ đề "%s".
                - Nếu chủ đề là một chất/nguyên tố cụ thể như Sắt/Fe, mọi câu phải nhắc trực tiếp tới chất đó, hợp chất của nó, ứng dụng hoặc phản ứng của nó.
                - Không đưa câu hỏi chung chung lệch chủ đề, ví dụ đề Sắt thì không hỏi riêng về biến đổi vật lí/hóa học nếu không gắn với sắt.
                - Sắp xếp questions theo thứ tự: toàn bộ MULTIPLE_CHOICE trước, sau đó mới đến ESSAY và LAB_APPLICATION.
                - Khoảng 60-70%% số câu là MULTIPLE_CHOICE; phần còn lại là ESSAY hoặc LAB_APPLICATION.
                - Có câu tính toán định lượng phù hợp chương trình, ví dụ tính khối lượng, số mol, thể tích khí hoặc lượng chất theo phương trình hóa học nếu chủ đề cho phép.
                - Theo chương trình mới, nếu dùng "điều kiện chuẩn", "đkc" hoặc "đktc" thì lấy V_m = 24,79 L/mol ở 25°C và 1 bar.
                - Chỉ dùng 22,4 L/mol khi câu hỏi ghi rõ 0°C, 1 atm hoặc nói theo quy ước cũ.
                - Nếu độ khó là HARD: phải có câu nhiều bước, dữ kiện nhiễu hợp lí, tính theo phương trình, nhận biết chất dư/chất hết hoặc suy luận từ nhiều dữ kiện; không được chỉ hỏi định nghĩa đơn giản.
                - Nếu độ khó là MIXED: chia đều câu dễ, trung bình, khó; ít nhất 25%% câu ở mức khó.
                - Distractors trong trắc nghiệm phải sát lỗi sai thường gặp, không quá lộ.
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
                difficultyInstruction(request.getDifficulty()),
                curriculumContext,
                cleanTopic(request.getTopic())
        );
    }

    private String difficultyInstruction(ExamDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "- EASY: hỏi nhận biết/thông hiểu cơ bản, mỗi câu chỉ cần 1 bước suy luận.";
            case MEDIUM -> "- MEDIUM: có vận dụng, có câu tính toán 2 bước, có nhiễu gần lỗi sai thường gặp.";
            case HARD -> """
                    - HARD: đề phải khó rõ rệt nhưng vẫn đúng THCS.
                    - Ít nhất một nửa câu trắc nghiệm cần 2 bước suy luận hoặc tính toán.
                    - Câu tự luận/vận dụng cần nhiều bước: đổi mol, cân bằng phương trình, xác định tỉ lệ mol, chất dư/chất hết hoặc suy luận chất.
                    - Không dùng câu hỏi định nghĩa quá dễ trừ khi là câu mở đầu.
                    """;
            case MIXED -> """
                    - MIXED: 30% dễ, 40% trung bình, 30% khó.
                    - Câu khó phải có tính toán hoặc suy luận nhiều dữ kiện.
                    """;
        };
    }

    private int questionCountForExamType(ExamType examType) {
        if (examType == ExamType.QUIZ_15_MIN) {
            return 8;
        }
        if (examType == ExamType.FORTY_FIVE_MINUTES) {
            return 14;
        }
        return 20;
    }

    private int calculationQuestionCountForExamType(ExamType examType) {
        if (examType == ExamType.QUIZ_15_MIN) {
            return 2;
        }
        if (examType == ExamType.FORTY_FIVE_MINUTES) {
            return 4;
        }
        return 6;
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
            response.setQuestions(orderExamQuestions(response.getQuestions()));
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

    private List<GeneratedQuestionDTO> orderExamQuestions(List<GeneratedQuestionDTO> questions) {
        return questions.stream()
                .sorted(Comparator.comparingInt(question -> switch (question.getType()) {
                    case MULTIPLE_CHOICE -> 0;
                    case ESSAY -> 1;
                    case LAB_APPLICATION -> 2;
                }))
                .toList();
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

    private ChatAnswerParts toChatAnswerParts(String rawAnswer) {
        String sanitized = sanitizeAiAnswer(rawAnswer);
        if (sanitized.isBlank()) {
            return new ChatAnswerParts("", "");
        }

        int delimiterIndex = sanitized.indexOf(TTS_EXPLANATION_DELIMITER);
        if (delimiterIndex >= 0) {
            String visible = sanitizeAiAnswer(sanitized.substring(0, delimiterIndex));
            String speech = sanitizeAiAnswer(sanitized.substring(delimiterIndex + TTS_EXPLANATION_DELIMITER.length()));
            visible = cleanupChatSectionLabel(visible);
            speech = cleanupChatSectionLabel(speech);
            if (visible.isBlank()) {
                visible = conciseVisibleAnswer(speech);
            }
            if (speech.isBlank()) {
                speech = visible;
            }
            return new ChatAnswerParts(conciseVisibleAnswer(visible), speech);
        }

        return new ChatAnswerParts(conciseVisibleAnswer(sanitized), sanitized);
    }

    private String conciseVisibleAnswer(String answer) {
        String cleaned = cleanupChatSectionLabel(sanitizeAiAnswer(answer));
        if (cleaned.isBlank()) {
            return "";
        }

        List<String> lines = Arrays.stream(cleaned.split("\\R+"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .limit(MAX_VISIBLE_CHAT_LINES)
                .toList();

        String joined = String.join("\n", lines);
        if (joined.length() <= MAX_VISIBLE_CHAT_CHARS) {
            return joined;
        }

        int cutAt = joined.lastIndexOf('.', MAX_VISIBLE_CHAT_CHARS);
        if (cutAt < MAX_VISIBLE_CHAT_CHARS / 2) {
            cutAt = joined.lastIndexOf('\n', MAX_VISIBLE_CHAT_CHARS);
        }
        if (cutAt < MAX_VISIBLE_CHAT_CHARS / 2) {
            cutAt = MAX_VISIBLE_CHAT_CHARS;
        }
        return joined.substring(0, cutAt).trim();
    }

    private String cleanupChatSectionLabel(String value) {
        return value
                .replaceAll("(?im)^\\s*(PHAN_HIEN_THI|PHAN_DOC|HIEN_THI|LOI_DOC|VISIBLE|SPEECH|SHORT_ANSWER|DETAILED_EXPLANATION)\\s*:?\\s*$", "")
                .replaceAll("(?im)^\\s*(Phan hien thi|Phan doc|Tra loi ngan|Giai thich ky|Noi dung doc)\\s*:?\\s*$", "")
                .trim();
    }

    private record ChatAnswerParts(String answer, String speechText) {
        private String toStoredContent() {
            if (speechText == null || speechText.isBlank() || speechText.equals(answer)) {
                return answer == null ? "" : answer;
            }
            return (answer == null ? "" : answer) + "\n\n" + TTS_EXPLANATION_DELIMITER + "\n" + speechText;
        }
    }

    private String sanitizeAiAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        String cleaned = answer
                .replaceAll("(?is)<sub[^>]*>(.*?)</sub>", "_{$1}")
                .replaceAll("(?is)<sup[^>]*>(.*?)</sup>", "^{$1}")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?is)<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
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
        ChatAnswerParts answerParts = message.getRole() == AiMessageRole.ASSISTANT
                ? toChatAnswerParts(message.getContent())
                : null;
        return AiChatMessageResponse.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(answerParts == null ? message.getContent() : answerParts.answer())
                .speechText(answerParts == null ? null : answerParts.speechText())
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
        String raw = CHAT_CACHE_VERSION + "|" + grade + "|" + bookType + "|" + normalizeForCache(cleanTopic(topic)) + "|" + normalizedQuestion;
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
