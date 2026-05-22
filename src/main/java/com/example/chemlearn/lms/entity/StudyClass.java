package com.example.chemlearn.lms.entity;

import com.example.chemlearn.core.entity.Teacher;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.*;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "classes")
public class StudyClass {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;


    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "class_code", nullable = false, unique = true, length = 6)
    private String classCode;

    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    @Column(name = "class_type")
    private String classType;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "studyClassField", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyClassEnrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "studyClassField", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyClassAssignment> assignments = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(name = "class_chapters",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "chapter_id"))
    private List<Chapter> chapters = new ArrayList<>();
}

