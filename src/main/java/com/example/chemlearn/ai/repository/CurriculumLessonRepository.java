package com.example.chemlearn.ai.repository;

import com.example.chemlearn.ai.entity.CurriculumLesson;
import com.example.chemlearn.ai.enums.BookType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CurriculumLessonRepository extends JpaRepository<CurriculumLesson, UUID> {
    @Query("""
            select cl
            from CurriculumLesson cl
            left join fetch cl.lesson lesson
            left join fetch cl.lab lab
            where cl.gradeLevel = :grade
                and cl.bookType = :bookType
                and cl.published = true
                and (
                    :topic is null
                    or lower(cl.topic) like lower(concat('%', :topic, '%'))
                    or lower(cl.title) like lower(concat('%', :topic, '%'))
                    or lower(coalesce(cl.content, '')) like lower(concat('%', :topic, '%'))
                )
            order by cl.createdAt asc
            """)
    List<CurriculumLesson> searchContext(@Param("grade") Integer grade,
                                         @Param("bookType") BookType bookType,
                                         @Param("topic") String topic);
}
