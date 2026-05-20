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
@Table(name = "student_pet_fragments", uniqueConstraints = {
        @UniqueConstraint(name = "student_pet_fragments_student_id_species_id_key", columnNames = {"student_id", "species_id"})
})
public class StudentPetFragment {
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

    @ColumnDefault("0")
    @Column(name = "amount", nullable = false)
    private Integer amount = 0;
}
