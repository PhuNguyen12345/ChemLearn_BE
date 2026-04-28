package com.example.chemlearn.lms.entity;

import com.example.chemlearn.lms.enums.QuestionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.*;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(name = "content", nullable = false, length = Integer.MAX_VALUE)
    private String content;

    @ColumnDefault("'SINGLE_CHOICE'")
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", length = 50)
    private QuestionType questionType;

    @Column(name = "explanation", length = Integer.MAX_VALUE)
    private String explanation;

    @ColumnDefault("0")
    @Column(name = "order_index")
    private Integer orderIndex;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void validateBelongsTo() {
        boolean hasLesson = lesson != null;
        boolean hasQuiz = quiz != null;
        if ((hasLesson && hasQuiz) || (!hasLesson && !hasQuiz)) {
            throw new IllegalArgumentException("Question must belong to exactly one: Lesson OR Quiz");
        }
    }
}

