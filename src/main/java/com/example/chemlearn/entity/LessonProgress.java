package com.example.chemlearn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "lesson_progress", uniqueConstraints = {
        @UniqueConstraint(name = "lesson_progress_student_id_lesson_id_key", columnNames = {"student_id", "lesson_id"})
})
public class LessonProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ColumnDefault("false")
    @Column(name = "is_completed")
    private Boolean isCompleted;

    @ColumnDefault("true")
    @Column(name = "is_locked")
    private Boolean isLocked;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

}