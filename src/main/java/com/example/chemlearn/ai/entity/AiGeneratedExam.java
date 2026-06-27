package com.example.chemlearn.ai.entity;

import com.example.chemlearn.ai.enums.BookType;
import com.example.chemlearn.ai.enums.ExamDifficulty;
import com.example.chemlearn.ai.enums.ExamType;
import com.example.chemlearn.core.entity.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "ai_generated_exams")
public class AiGeneratedExam {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "grade_level", nullable = false)
    private Integer grade;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_type", nullable = false, length = 20)
    private BookType bookType;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 40)
    private ExamType examType;

    @Column(name = "topic", nullable = false, length = 255)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private ExamDifficulty difficulty;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "questions", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> questions;

    @Column(name = "answer_key", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> answerKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
