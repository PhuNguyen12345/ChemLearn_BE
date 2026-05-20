package com.example.chemlearn.lms.dto.student;

import lombok.Data;
import java.util.UUID;

@Data
public class EnrolledClassDTO {
    private UUID classId;
    private String className;
    private String teacherName;
    private int progress;
}
