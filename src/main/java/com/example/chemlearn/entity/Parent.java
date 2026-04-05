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
@Table(name = "parents")
@DiscriminatorValue("PARENT")
@PrimaryKeyJoinColumn(name = "user_id")
public class Parent extends User {

    @Size(max = 20)
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Size(max = 100)
    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @OneToMany(mappedBy = "parent")
    private Set<Student> students = new LinkedHashSet<>();

}