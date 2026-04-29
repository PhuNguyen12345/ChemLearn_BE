package com.example.chemlearn.lab.entity;

import com.example.chemlearn.core.entity.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_lab_progress")
public class UserLabProgress {
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
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    @ColumnDefault("'UNCOMPLETED'")
    @Column(name = "status", length = 50)
    private String status;

    @ColumnDefault("0")
    @Column(name = "progress_percent")
    private Integer progressPercent;

    @ColumnDefault("0")
    @Column(name = "current_score")
    private Integer currentScore;

    @Column(name = "completed_actions")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> completedActions;

    @Column(name = "current_workspace")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> currentWorkspace;

    @Column(name = "viewport")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> viewport;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @ColumnDefault("false")
    @Column(name = "is_finished")
    private Boolean isFinished;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "last_edited_at")
    private Instant lastEditedAt;

    @Override
    public String toString() {
        return "UserLabProgress{" +
                "id=" + id +
                ", student=" + student +
                ", lab=" + lab +
                ", status='" + status + '\'' +
                ", progressPercent=" + progressPercent +
                ", currentScore=" + currentScore +
                ", completedActions=" + completedActions +
                ", currentWorkspace=" + currentWorkspace +
                ", viewport=" + viewport +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", isFinished=" + isFinished +
                ", startedAt=" + startedAt +
                ", submittedAt=" + submittedAt +
                ", lastEditedAt=" + lastEditedAt +
                '}';
    }
}