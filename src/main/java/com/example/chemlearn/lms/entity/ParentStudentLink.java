package com.example.chemlearn.lms.entity;
import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.Parent;

import com.example.chemlearn.core.entity.User;
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
@Table(
        name = "parent_student_links",
        uniqueConstraints = @UniqueConstraint(columnNames = {"parent_id", "student_id"})
)
public class ParentStudentLink {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    @JsonIgnore
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private User student;
}

