package com.example.chemlearn.ai.repository;

import com.example.chemlearn.ai.entity.StudentTopicMastery;
import com.example.chemlearn.ai.enums.BookType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StudentTopicMasteryRepository extends JpaRepository<StudentTopicMastery, UUID> {
    @Query("""
            select stm
            from StudentTopicMastery stm
            where stm.student.id = :studentId
                and stm.grade = :grade
                and stm.bookType = :bookType
                and lower(stm.topic) = lower(:topic)
            """)
    Optional<StudentTopicMastery> findExisting(@Param("studentId") UUID studentId,
                                               @Param("grade") Integer grade,
                                               @Param("bookType") BookType bookType,
                                               @Param("topic") String topic);
}
