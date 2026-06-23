package com.example.chemlearn.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.chemlearn.lms.enums.MaterialScope;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.example.chemlearn.core.entity.User;

import java.time.Instant;
import java.util.*;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "chapters")
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    @ColumnDefault("0")
    @Column(name = "order_index")
    private Integer orderIndex;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "published")
    private Boolean published;

    @ColumnDefault("false")
    @Column(name = "need_purchase", nullable = false)
    private Boolean needPurchase = false;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_scope", nullable = false, length = 30)
    private MaterialScope materialScope = MaterialScope.GLOBAL;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "owner_class_id")
    private StudyClass ownerClass;

    @JsonIgnore
    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "chapters")
    private List<StudyClass> studyClasses = new ArrayList<>();

}
