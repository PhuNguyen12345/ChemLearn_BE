package com.example.chemlearn.lms.entity;

import com.example.chemlearn.lab.entity.Lab;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lessons")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "lab_id")
    private Lab lab;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "video_url", length = Integer.MAX_VALUE)
    private String videoUrl;

    @Column(name = "text_content", length = Integer.MAX_VALUE)
    private String textContent;

    @ColumnDefault("0")
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @ColumnDefault("0")
    @Column(name = "order_index")
    private Integer orderIndex;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

}