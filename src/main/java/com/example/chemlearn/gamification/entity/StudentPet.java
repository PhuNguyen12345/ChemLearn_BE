package com.example.chemlearn.gamification.entity;

import com.example.chemlearn.core.entity.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "student_pets", uniqueConstraints = {
        @UniqueConstraint(name = "student_pets_student_id_species_id_key", columnNames = {"student_id", "species_id"})
})
public class StudentPet {
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
    @JoinColumn(name = "species_id", nullable = false)
    private PetSpecies species;

    @ColumnDefault("1")
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @ColumnDefault("0")
    @Column(name = "experience", nullable = false)
    private Integer experience = 0;

    @ColumnDefault("1")
    @Column(name = "star_level", nullable = false)
    private Integer starLevel = 1;
}
