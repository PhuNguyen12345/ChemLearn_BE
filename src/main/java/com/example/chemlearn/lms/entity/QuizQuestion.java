package com.example.chemlearn.lms.entity;

import com.example.chemlearn.lms.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "quiz_questions")
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonIgnore
    private Quiz quiz;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", length = 50, nullable = false)
    private QuestionType questionType = QuestionType.SINGLE_CHOICE;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "option_a", columnDefinition = "TEXT")
    private String optionA;

    @Column(name = "option_b", columnDefinition = "TEXT")
    private String optionB;

    @Column(name = "option_c", columnDefinition = "TEXT")
    private String optionC;

    @Column(name = "option_d", columnDefinition = "TEXT")
    private String optionD;

    @Column(name = "correct_option", length = 255)
    private String correctOption;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
}
