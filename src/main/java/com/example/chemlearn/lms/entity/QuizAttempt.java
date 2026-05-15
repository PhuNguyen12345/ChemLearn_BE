package com.example.chemlearn.lms.entity;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.lms.enums.AttemptStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "quiz_attempts")
@DynamicInsert
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @ColumnDefault("0")
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score = BigDecimal.ZERO;

    @ColumnDefault("0")
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions = 0;

    @ColumnDefault("0")
    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers = 0;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "started_at")
    private Instant startedAt = Instant.now();

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @PrePersist
    @PreUpdate
    private void validateBelongsTo() {
        boolean hasQuiz = quiz != null;
        boolean hasLesson = lesson != null;
        if ((hasQuiz && hasLesson) || (!hasQuiz && !hasLesson)) {
            throw new IllegalArgumentException("QuizAttempt must belong to exactly one: Quiz OR Lesson");
        }
        if (status == null) {
            status = AttemptStatus.IN_PROGRESS;
        }
        if (totalQuestions == null) {
            totalQuestions = 0;
        }
        if (correctAnswers == null) {
            correctAnswers = 0;
        }
        if (startedAt == null) {
            startedAt = Instant.now();
        }
    }

}
