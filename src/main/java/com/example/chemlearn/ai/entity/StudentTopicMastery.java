package com.example.chemlearn.ai.entity;

import com.example.chemlearn.ai.enums.BookType;
import com.example.chemlearn.ai.enums.MasteryLevel;
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
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "student_topic_mastery")
public class StudentTopicMastery {
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

    @Column(name = "topic", nullable = false, length = 255)
    private String topic;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    @Column(name = "last_accuracy", nullable = false)
    private Double lastAccuracy;

    @Enumerated(EnumType.STRING)
    @Column(name = "mastery_level", nullable = false, length = 20)
    private MasteryLevel masteryLevel;

    @Column(name = "wrong_topics")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> wrongTopics;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }
}
