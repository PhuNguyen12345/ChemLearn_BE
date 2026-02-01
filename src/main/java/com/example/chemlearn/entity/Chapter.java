package com.example.chemlearn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "chapters")
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 200)
    @NotNull
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @NotNull
    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    @ColumnDefault("0")
    @Column(name = "order_index")
    private Integer orderIndex;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "chapter")
    private Set<Lesson> lessons = new LinkedHashSet<>();

}