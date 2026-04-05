package com.example.chemlearn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "lessons")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Size(max = 200)
    @NotNull
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Size(max = 50)
    @NotNull
    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "video_url", length = Integer.MAX_VALUE)
    private String videoUrl;

    @Column(name = "text_content", length = Integer.MAX_VALUE)
    private String textContent;

    @Column(name = "lab_config")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> labConfig;

    @ColumnDefault("0")
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @ColumnDefault("0")
    @Column(name = "order_index")
    private Integer orderIndex;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "lesson")
    private Set<LessonProgress> lessonProgresses = new LinkedHashSet<>();

    @OneToMany(mappedBy = "lesson")
    private Set<Question> questions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "lesson")
    private Set<QuizAttempt> quizAttempts = new LinkedHashSet<>();

}