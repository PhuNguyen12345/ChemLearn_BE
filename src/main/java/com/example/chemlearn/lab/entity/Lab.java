package com.example.chemlearn.lab.entity;

import com.example.chemlearn.lab.enums.Difficulty;
import com.example.chemlearn.lab.enums.LabCategory;
import com.example.chemlearn.lab.enums.LabType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lab")
public class Lab {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "category", length = 50)
    @Enumerated(EnumType.STRING)
    private LabCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 20)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private LabType type;

    @Column(name = "tag", length = 50)
    private String tag;

    @Column(name = "gradient", length = 100)
    private String gradient;

    @Column(name = "icon_color", length = 50)
    private String iconColor;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @ColumnDefault("0")
    @Column(name = "max_score")
    private Integer maxScore;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "author_id")
    private UUID authorId;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToOne(mappedBy = "lab", cascade = CascadeType.ALL)
    private LabConfiguration labConfiguration;

    @Override
    public String toString() {
        return "Lab{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category=" + category +
                ", difficulty=" + difficulty +
                ", type=" + type +
                ", tag='" + tag + '\'' +
                ", gradient='" + gradient + '\'' +
                ", iconColor='" + iconColor + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", maxScore=" + maxScore +
                ", createdAt=" + createdAt +
                ", authorId=" + authorId +
                ", updatedAt=" + updatedAt +
                ", labConfiguration=" + labConfiguration +
                '}';
    }
}