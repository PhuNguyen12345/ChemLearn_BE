package com.example.chemlearn.gamification.entity;

import com.example.chemlearn.core.entity.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "student_node_progress", uniqueConstraints = {
        @UniqueConstraint(name = "student_node_progress_student_id_node_id_key", columnNames = {"student_id", "node_id"})
})
public class StudentNodeProgress {
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
    @JoinColumn(name = "node_id", nullable = false)
    private MapNode node;

    @ColumnDefault("false")
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @ColumnDefault("0")
    @Column(name = "stars", nullable = false)
    private Integer stars = 0;

    @Column(name = "completed_at")
    private Instant completedAt;
}
