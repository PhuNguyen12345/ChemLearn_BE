package com.example.chemlearn.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "teachers")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", nullable = false)
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User users;

    @Column(name = "bio", length = Integer.MAX_VALUE)
    private String bio;

    @Column(name = "specialization", length = 100)
    private String specialization;

    @Column(name = "degree", length = 100)
    private String degree;

    @Column(name = "workplace")
    private String workplace;

}