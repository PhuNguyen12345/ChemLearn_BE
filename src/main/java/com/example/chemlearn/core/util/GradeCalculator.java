package com.example.chemlearn.core.util;

import java.time.LocalDate;

/**
 * Utility class để tính toán lớp học động dựa trên target_graduation_year.
 *
 * <h3>Quy ước năm học Việt Nam:</h3>
 * <ul>
 *     <li>Năm học mới bắt đầu từ tháng 7.</li>
 *     <li>Cấp 2 (THCS) gồm lớp 6 → lớp 9.</li>
 *     <li>Tốt nghiệp = hoàn thành lớp 9.</li>
 * </ul>
 *
 * <h3>Công thức tính lớp hiện tại:</h3>
 * <pre>
 * Nếu tháng >= 7: currentGrade = 9 - (targetYear - currentYear) + 1
 * Nếu tháng <  7: currentGrade = 9 - (targetYear - currentYear)
 * </pre>
 *
 * <h3>Ví dụ (chạy ngày 26/06/2026, tháng 6 < 7):</h3>
 * <table>
 *     <tr><th>targetGraduationYear</th><th>Kết quả</th><th>Status</th></tr>
 *     <tr><td>2027</td><td>Lớp 8</td><td>ACTIVE</td></tr>
 *     <tr><td>2026</td><td>Lớp 9</td><td>ACTIVE</td></tr>
 *     <tr><td>2025</td><td>Lớp 10</td><td>GRADUATED</td></tr>
 *     <tr><td>2030</td><td>Lớp 5</td><td>NOT_YET_ENROLLED</td></tr>
 * </table>
 */
public final class GradeCalculator {

    /** Tháng bắt đầu năm học mới (Tháng 7 - Việt Nam). */
    public static final int ACADEMIC_YEAR_START_MONTH = 7;

    /** Lớp thấp nhất cấp THCS. */
    public static final int MIN_GRADE = 6;

    /** Lớp cao nhất cấp THCS. */
    public static final int MAX_GRADE = 9;

    /**
     * Trạng thái học tập của học sinh trong hệ THCS.
     */
    public enum StudentStatus {
        /** Đang học trong hệ THCS (lớp 6-9). */
        ACTIVE,

        /** Đã hoàn thành chương trình THCS (currentGrade > 9). */
        GRADUATED,

        /** Chưa vào cấp THCS (currentGrade < 6). */
        NOT_YET_ENROLLED
    }

    private GradeCalculator() {
        // Utility class — không cho phép khởi tạo instance
    }

    // ===================================================================
    //  CORE CALCULATION METHODS
    // ===================================================================

    /**
     * Tính lớp hiện tại từ {@code target_graduation_year}.
     *
     * <p>Công thức:</p>
     * <ul>
     *     <li>Nếu tháng hiện tại >= 7: {@code grade = 9 - (targetYear - currentYear) + 1}</li>
     *     <li>Nếu tháng hiện tại <  7: {@code grade = 9 - (targetYear - currentYear)}</li>
     * </ul>
     *
     * @param targetGraduationYear năm dự kiến tốt nghiệp THCS
     * @return lớp hiện tại (có thể > 9 nếu đã tốt nghiệp, hoặc < 6 nếu chưa vào cấp 2)
     */
    public static int calculateCurrentGrade(int targetGraduationYear) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        if (currentMonth >= ACADEMIC_YEAR_START_MONTH) {
            // Đã bắt đầu năm học mới
            return MAX_GRADE - (targetGraduationYear - currentYear) + 1;
        } else {
            // Vẫn trong năm học cũ
            return MAX_GRADE - (targetGraduationYear - currentYear);
        }
    }

    /**
     * Tính {@code target_graduation_year} từ {@code grade_level} hiện tại.
     *
     * <p>Đây là công thức ngược của {@link #calculateCurrentGrade(int)}.</p>
     *
     * @param gradeLevel lớp hiện tại (6-9)
     * @return năm dự kiến tốt nghiệp THCS
     * @throws IllegalArgumentException nếu gradeLevel nằm ngoài phạm vi [6, 9]
     */
    public static int calculateTargetGraduationYear(int gradeLevel) {
        if (gradeLevel < MIN_GRADE || gradeLevel > MAX_GRADE) {
            throw new IllegalArgumentException(
                    "Grade level must be between " + MIN_GRADE + " and " + MAX_GRADE
                            + ", got: " + gradeLevel
            );
        }

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        if (currentMonth >= ACADEMIC_YEAR_START_MONTH) {
            // Đã bắt đầu năm học mới: student vừa lên lớp này
            return currentYear + (MAX_GRADE - gradeLevel) + 1;
        } else {
            // Vẫn trong năm học cũ
            return currentYear + (MAX_GRADE - gradeLevel);
        }
    }

    /**
     * Xác định trạng thái học sinh dựa trên lớp hiện tại.
     *
     * @param currentGrade lớp hiện tại (kết quả từ {@link #calculateCurrentGrade(int)})
     * @return {@link StudentStatus} tương ứng
     */
    public static StudentStatus determineStatus(int currentGrade) {
        if (currentGrade > MAX_GRADE) {
            return StudentStatus.GRADUATED;
        }
        if (currentGrade < MIN_GRADE) {
            return StudentStatus.NOT_YET_ENROLLED;
        }
        return StudentStatus.ACTIVE;
    }
}
