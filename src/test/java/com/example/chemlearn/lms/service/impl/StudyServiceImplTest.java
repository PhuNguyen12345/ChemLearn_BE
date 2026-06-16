package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.shared.principal.UserPrincipal;
import com.example.chemlearn.lms.dto.study.StudyChapterDTO;
import com.example.chemlearn.lms.entity.Chapter;
import com.example.chemlearn.lms.entity.Lesson;
import com.example.chemlearn.lms.enums.MaterialScope;
import com.example.chemlearn.lms.repository.ChapterRepository;
import com.example.chemlearn.lms.repository.LessonRepository;
import com.example.chemlearn.lms.repository.MiniQuizQuestionRepository;
import com.example.chemlearn.payment.entity.UserPackageEntitlement;
import com.example.chemlearn.payment.enums.EntitlementStatus;
import com.example.chemlearn.payment.repository.UserPackageEntitlementRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyServiceImplTest {

    private final ChapterRepository chapterRepository = mock(ChapterRepository.class);
    private final LessonRepository lessonRepository = mock(LessonRepository.class);
    private final MiniQuizQuestionRepository miniQuizQuestionRepository = mock(MiniQuizQuestionRepository.class);
    private final UserPackageEntitlementRepository entitlementRepository = mock(UserPackageEntitlementRepository.class);

    private final StudyServiceImpl service = new StudyServiceImpl(
            chapterRepository,
            lessonRepository,
            miniQuizQuestionRepository,
            entitlementRepository
    );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void lockedChapterIsVisibleWithoutAccessMetadataWhenStudentHasNoEntitlement() {
        UUID userId = UUID.randomUUID();
        authenticate(userId);
        Chapter chapter = chapter(6, true);
        Lesson lesson = lesson(chapter);

        when(chapterRepository.findByPublishedTrueAndMaterialScopeOrderByOrderIndexAsc(MaterialScope.GLOBAL))
                .thenReturn(List.of(chapter));
        when(entitlementRepository.findByUserIdAndPackageCodeAndStatus(userId, "GRADE_6", EntitlementStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(lessonRepository.findByChapterIdAndPublishedTrueAndMaterialScopeOrderByOrderIndexAsc(
                chapter.getId(), MaterialScope.GLOBAL))
                .thenReturn(List.of(lesson));

        List<StudyChapterDTO> chapters = service.getChaptersWithLessons();

        assertThat(chapters).hasSize(1);
        StudyChapterDTO dto = chapters.getFirst();
        assertThat(dto.getGradeLevel()).isEqualTo(6);
        assertThat(dto.getNeedPurchase()).isTrue();
        assertThat(dto.getHasAccess()).isFalse();
        assertThat(dto.getRequiredPackageCode()).isEqualTo("GRADE_6");
        assertThat(dto.getLessons()).hasSize(1);
    }

    @Test
    void lockedLessonRequiresMatchingCurrentEntitlement() {
        UUID userId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        authenticate(userId);
        Chapter chapter = chapter(6, true);
        Lesson lesson = lesson(chapter);
        lesson.setId(lessonId);

        when(lessonRepository.findByIdAndPublishedTrueAndMaterialScope(lessonId, MaterialScope.GLOBAL))
                .thenReturn(Optional.of(lesson));
        when(entitlementRepository.findByUserIdAndPackageCodeAndStatus(userId, "GRADE_6", EntitlementStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLessonDetail(lessonId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void matchingActiveEntitlementCanOpenLockedLesson() {
        UUID userId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        authenticate(userId);
        Chapter chapter = chapter(6, true);
        Lesson lesson = lesson(chapter);
        lesson.setId(lessonId);
        lesson.setTextContent("Content");
        UserPackageEntitlement entitlement = entitlement(LocalDateTime.now().plusDays(10));

        when(lessonRepository.findByIdAndPublishedTrueAndMaterialScope(lessonId, MaterialScope.GLOBAL))
                .thenReturn(Optional.of(lesson));
        when(entitlementRepository.findByUserIdAndPackageCodeAndStatus(userId, "GRADE_6", EntitlementStatus.ACTIVE))
                .thenReturn(Optional.of(entitlement));
        when(miniQuizQuestionRepository.findByLessonIdOrderByIdAsc(lessonId)).thenReturn(List.of());

        assertThat(service.getLessonDetail(lessonId).getId()).isEqualTo(lessonId);
    }

    @Test
    void expiredEntitlementCannotOpenLockedLesson() {
        UUID userId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        authenticate(userId);
        Chapter chapter = chapter(6, true);
        Lesson lesson = lesson(chapter);
        lesson.setId(lessonId);

        when(lessonRepository.findByIdAndPublishedTrueAndMaterialScope(lessonId, MaterialScope.GLOBAL))
                .thenReturn(Optional.of(lesson));
        when(entitlementRepository.findByUserIdAndPackageCodeAndStatus(userId, "GRADE_6", EntitlementStatus.ACTIVE))
                .thenReturn(Optional.of(entitlement(LocalDateTime.now().minusDays(1))));

        assertThatThrownBy(() -> service.getLessonDetail(lessonId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void cancelledEntitlementCannotOpenLockedLesson() {
        UUID userId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        authenticate(userId);
        Chapter chapter = chapter(6, true);
        Lesson lesson = lesson(chapter);
        lesson.setId(lessonId);

        when(lessonRepository.findByIdAndPublishedTrueAndMaterialScope(lessonId, MaterialScope.GLOBAL))
                .thenReturn(Optional.of(lesson));
        when(entitlementRepository.findByUserIdAndPackageCodeAndStatus(userId, "GRADE_6", EntitlementStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLessonDetail(lessonId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void publicChapterLessonDoesNotRequireEntitlement() {
        UUID userId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        authenticate(userId);
        Chapter chapter = chapter(6, false);
        Lesson lesson = lesson(chapter);
        lesson.setId(lessonId);
        lesson.setTextContent("Content");

        when(lessonRepository.findByIdAndPublishedTrueAndMaterialScope(lessonId, MaterialScope.GLOBAL))
                .thenReturn(Optional.of(lesson));
        when(miniQuizQuestionRepository.findByLessonIdOrderByIdAsc(lessonId)).thenReturn(List.of());

        assertThat(service.getLessonDetail(lessonId).getId()).isEqualTo(lessonId);
    }

    private void authenticate(UUID userId) {
        UserPrincipal principal = new UserPrincipal(userId, "student", "password", "ROLE_STUDENT", true, List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    private Chapter chapter(Integer gradeLevel, boolean needPurchase) {
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setTitle("Chapter");
        chapter.setDescription("Description");
        chapter.setGradeLevel(gradeLevel);
        chapter.setPublished(true);
        chapter.setNeedPurchase(needPurchase);
        chapter.setMaterialScope(MaterialScope.GLOBAL);
        return chapter;
    }

    private Lesson lesson(Chapter chapter) {
        Lesson lesson = new Lesson();
        lesson.setId(UUID.randomUUID());
        lesson.setChapter(chapter);
        lesson.setTitle("Lesson");
        lesson.setDurationMinutes(10);
        lesson.setPublished(true);
        lesson.setMaterialScope(MaterialScope.GLOBAL);
        return lesson;
    }

    private UserPackageEntitlement entitlement(LocalDateTime endAt) {
        UserPackageEntitlement entitlement = new UserPackageEntitlement();
        entitlement.setStatus(EntitlementStatus.ACTIVE);
        entitlement.setEndAt(endAt);
        return entitlement;
    }
}
