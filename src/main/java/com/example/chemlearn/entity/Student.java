package com.example.chemlearn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "students")
@DiscriminatorValue("STUDENT")
@PrimaryKeyJoinColumn(name="user_id")
public class Student extends User {

    @NotNull
    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    @ColumnDefault("0")
    @Column(name = "total_points")
    private Integer totalPoints;

    @ColumnDefault("0")
    @Column(name = "current_streak")
    private Integer currentStreak;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "parent_id")
    private Parent parent;

    @Size(max = 255)
    @Column(name = "school_name")
    private String schoolName;

    @OneToMany(mappedBy = "student")
    private Set<ClassEnrollment> classEnrollments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "student")
    private Set<LessonProgress> lessonProgresses = new LinkedHashSet<>();

    @OneToMany(mappedBy = "student")
    private Set<QuizAttempt> quizAttempts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "student")
    private Set<UserBadge> userBadges = new LinkedHashSet<>();

}