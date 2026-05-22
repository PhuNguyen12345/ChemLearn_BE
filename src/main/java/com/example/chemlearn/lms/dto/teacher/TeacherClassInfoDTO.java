package com.example.chemlearn.lms.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;
import com.example.chemlearn.lms.dto.response.ChapterResponse;

@Data
@AllArgsConstructor
public class TeacherClassInfoDTO {
    private UUID id;
    private String name;
    private String description;
    private String classCode;
    private Integer gradeLevel;
    private String classType;
    private List<StudentBrief> students;
    private List<ChapterResponse> chapters;

    @Data
    @AllArgsConstructor
    public static class StudentBrief {
        private UUID id;
        private String username;
        private String email;
        private String accountLink;
    }
}
