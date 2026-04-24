package com.example.chemlearn.core.entity;

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
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", nullable = false)
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User users;

    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    @ColumnDefault("0")
    @Column(name = "total_points")
    private Integer totalPoints;

    @ColumnDefault("0")
    @Column(name = "current_streak")
    private Integer currentStreak;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "parent_id")
    private Parent parent;

    @Column(name = "school_name")
    private String schoolName;

}