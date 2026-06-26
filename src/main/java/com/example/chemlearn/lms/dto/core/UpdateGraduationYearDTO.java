package com.example.chemlearn.lms.dto.core;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO cho Admin override target_graduation_year của học sinh.
 * Dùng trong trường hợp đặc biệt: lưu ban, chuyển trường, điều chỉnh thủ công.
 */
@Getter
@Setter
public class UpdateGraduationYearDTO {

    @NotNull(message = "Target graduation year is required")
    @Min(value = 2020, message = "Target graduation year must be 2020 or later")
    @Max(value = 2050, message = "Target graduation year must be 2050 or earlier")
    private Integer targetGraduationYear;
}
