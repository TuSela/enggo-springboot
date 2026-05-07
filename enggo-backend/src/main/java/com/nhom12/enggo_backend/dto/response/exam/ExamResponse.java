package com.nhom12.enggo_backend.dto.response.exam;

import com.nhom12.enggo_backend.dto.response.UserMinimalResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamResponse {
    Integer id;
    String title;
    String examType;
    Byte difficulty;
    Integer totalQuestions;
    Integer durationMinutes;
    Boolean active;
    private UserMinimalResponse createdBy;
    List<ThemeResponse> themes;
    List<SkillResponse> skills;
}
