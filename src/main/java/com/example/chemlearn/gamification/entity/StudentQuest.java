package com.example.chemlearn.gamification.entity;

import com.example.chemlearn.core.entity.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "student_quests", uniqueConstraints = {
        @UniqueConstraint(name = "student_quests_student_id_quest_id_assigned_date_key", columnNames = {"student_id", "quest_id", "assigned_date"})
})
public class StudentQuest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @ColumnDefault("0")
    @Column(name = "current_progress")
    private Integer currentProgress;

    @ColumnDefault("false")
    @Column(name = "is_claimed")
    private Boolean isClaimed;

    @ColumnDefault("CURRENT_DATE")
    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

}