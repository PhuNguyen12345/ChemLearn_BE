package com.example.chemlearn.lms.entity;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.lms.enums.LeaderboardCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "ranking_history", indexes = {
        @Index(name = "idx_ranking_history_date_cat", columnList = "record_date, category")
})
public class RankingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private LeaderboardCategory category;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "rank_value", nullable = false)
    private Integer rankValue;

    @Column(name = "score")
    private Integer score;
}
