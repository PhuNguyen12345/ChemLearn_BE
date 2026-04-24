package com.example.chemlearn.lms.entity;

import com.example.chemlearn.core.entity.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "quiz_attempts")
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

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @ColumnDefault("0")
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @ColumnDefault("0")
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @ColumnDefault("0")
    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

}