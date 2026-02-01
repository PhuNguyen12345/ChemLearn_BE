package com.example.chemlearn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "teachers")
@DiscriminatorValue("TEACHER")
@PrimaryKeyJoinColumn(name = "user_id")
public class Teacher extends User {

    @Column(name = "bio", length = Integer.MAX_VALUE)
    private String bio;

    @Size(max = 100)
    @Column(name = "specialization", length = 100)
    private String specialization;

    @Size(max = 100)
    @Column(name = "degree", length = 100)
    private String degree;

    @Size(max = 255)
    @Column(name = "workplace")
    private String workplace;

    @OneToMany(mappedBy = "teacher")
    private Set<Class> classes = new LinkedHashSet<>();

}