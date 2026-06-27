package com.example.chemlearn.ai.entity;

import com.example.chemlearn.ai.enums.BookType;
import com.example.chemlearn.lab.entity.Lab;
import com.example.chemlearn.lms.entity.Lesson;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "curriculum_lessons")
public class CurriculumLesson {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_type", nullable = false, length = 20)
    private BookType bookType;

    @Column(name = "topic", nullable = false, length = 255)
    private String topic;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", length = Integer.MAX_VALUE)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "lab_id")
    private Lab lab;

    @Column(name = "published", nullable = false)
    private Boolean published = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
