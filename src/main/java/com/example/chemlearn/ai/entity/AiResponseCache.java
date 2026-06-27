package com.example.chemlearn.ai.entity;

import com.example.chemlearn.ai.enums.BookType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "ai_response_cache", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ai_response_cache_key", columnNames = "cache_key")
})
public class AiResponseCache {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "cache_key", nullable = false, length = 128)
    private String cacheKey;

    @Column(name = "normalized_question", nullable = false, length = Integer.MAX_VALUE)
    private String normalizedQuestion;

    @Column(name = "grade_level", nullable = false)
    private Integer grade;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_type", nullable = false, length = 20)
    private BookType bookType;

    @Column(name = "topic", length = 255)
    private String topic;

    @Column(name = "answer", nullable = false, length = Integer.MAX_VALUE)
    private String answer;

    @Column(name = "suggested_labs")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> suggestedLabs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
