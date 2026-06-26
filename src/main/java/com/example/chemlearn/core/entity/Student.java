package com.example.chemlearn.core.entity;

import com.example.chemlearn.core.util.GradeCalculator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", nullable = false)
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User users;

    /**
     * @deprecated Sử dụng {@link #getCurrentGrade()} thay thế.
     * Cột này được giữ lại cho tương thích ngược (Dual-Write strategy).
     * Giá trị tĩnh — KHÔNG tự cập nhật khi sang năm học mới.
     * Sẽ bị xóa trong phiên bản tương lai sau khi hoàn tất migration.
     */
    @Deprecated(since = "v2.0", forRemoval = true)
    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    /**
     * Năm dự kiến tốt nghiệp THCS (lớp 9).
     * Dùng để tính lớp hiện tại động theo thời gian thực.
     *
     * @see GradeCalculator#calculateCurrentGrade(int)
     */
    @Column(name = "target_graduation_year")
    private Integer targetGraduationYear;

    @ColumnDefault("0")
    @Column(name = "total_points")
    private Integer totalPoints;

    @ColumnDefault("0")
    @Column(name = "experience")
    private Integer experience = 0;

    @ColumnDefault("0")
    @Column(name = "current_streak")
    private Integer currentStreak;

    @ColumnDefault("0")
    @Column(name = "coins")
    private Integer coins = 0;

    @ColumnDefault("0")
    @Column(name = "pvp_wins")
    private Integer pvpWins = 0;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "parent_id")
    private Parent parent;

    @Column(name = "school_name")
    private String schoolName;

    // ========================
    // DYNAMIC GRADE METHODS
    // ========================

    /**
     * Tính lớp hiện tại ĐỘNG dựa trên thời gian thực của máy chủ.
     *
     * <p>Ưu tiên sử dụng {@code targetGraduationYear} để tính toán.
     * Nếu {@code targetGraduationYear} chưa có giá trị (dữ liệu chưa migrate),
     * sẽ fallback về {@code gradeLevel} cũ.</p>
     *
     * @return lớp hiện tại của học sinh, hoặc {@code null} nếu cả hai cột đều null
     */
    public Integer getCurrentGrade() {
        if (targetGraduationYear != null) {
            return GradeCalculator.calculateCurrentGrade(targetGraduationYear);
        }
        // Fallback cho dữ liệu chưa migrate
        return gradeLevel;
    }

    /**
     * Xác định trạng thái học tập hiện tại của học sinh.
     *
     * @return {@link GradeCalculator.StudentStatus} — ACTIVE, GRADUATED, hoặc NOT_YET_ENROLLED
     */
    public GradeCalculator.StudentStatus getStudentStatus() {
        Integer grade = getCurrentGrade();
        if (grade == null) {
            return GradeCalculator.StudentStatus.ACTIVE;
        }
        return GradeCalculator.determineStatus(grade);
    }

}