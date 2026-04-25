package com.example.chemlearn.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.chemlearn.core.entity.Account;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "question_bank_items")
public class QuestionBankItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnore
    private Account createdBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "option_a", nullable = false, columnDefinition = "TEXT")
    private String optionA;

    @Column(name = "option_b", nullable = false, columnDefinition = "TEXT")
    private String optionB;

    @Column(name = "option_c", nullable = false, columnDefinition = "TEXT")
    private String optionC;

    @Column(name = "option_d", nullable = false, columnDefinition = "TEXT")
    private String optionD;

    @Column(name = "correct_option", nullable = false, length = 1)
    private String correctOption;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
