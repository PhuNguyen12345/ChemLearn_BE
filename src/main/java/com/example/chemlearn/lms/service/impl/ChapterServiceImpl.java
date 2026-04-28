package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.lms.entity.Chapter;
import com.example.chemlearn.lms.repository.ChapterRepository;
import com.example.chemlearn.lms.service.ChapterService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChapterServiceImpl implements ChapterService {
    @Autowired
    private ChapterRepository chapterRepository;

    @Override
    public Chapter create(Chapter chapter) {
        return chapterRepository.save(chapter);
    }

    @Override
    public Optional<Chapter> findById(UUID id) {
        return chapterRepository.findAll().stream().filter(chapter -> id.equals(chapter.getId())).findFirst();
    }

    @Override
    public List<Chapter> findAll() {
        return chapterRepository.findAll();
    }

    @Override
    public List<Chapter> findByGradeLevel(Integer gradeLevel) {
        return chapterRepository.findAll().stream().filter(chapter -> gradeLevel == null || gradeLevel.equals(chapter.getGradeLevel())).toList();
    }

    @Override
    public Chapter update(UUID id, Chapter chapter) {
        Chapter existing = findById(id).orElseThrow(() -> new RuntimeException("Chapter not found"));
        if (chapter.getTitle() != null) existing.setTitle(chapter.getTitle());
        if (chapter.getDescription() != null) existing.setDescription(chapter.getDescription());
        if (chapter.getGradeLevel() != null) existing.setGradeLevel(chapter.getGradeLevel());
        if (chapter.getOrderIndex() != null) existing.setOrderIndex(chapter.getOrderIndex());
        return chapterRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        findById(id).ifPresent(chapterRepository::delete);
    }
}
