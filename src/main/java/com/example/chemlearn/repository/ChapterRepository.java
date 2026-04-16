package com.example.chemlearn.repository;

import com.example.chemlearn.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByPublishedTrueOrderByDisplayOrderAsc();
}
