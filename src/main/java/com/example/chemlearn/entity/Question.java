package com.example.chemlearn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @NotNull
    @Column(name = "content", nullable = false, length = Integer.MAX_VALUE)
    private String content;

    @Size(max = 50)
    @ColumnDefault("'SINGLE_CHOICE'")
    @Column(name = "question_type", length = 50)
    private String questionType;

    @Column(name = "explanation", length = Integer.MAX_VALUE)
    private String explanation;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "question")
    private Set<Answer> answers = new LinkedHashSet<>();

}