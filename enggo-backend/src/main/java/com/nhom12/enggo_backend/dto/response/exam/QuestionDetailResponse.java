package com.nhom12.enggo_backend.dto.response.exam;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nhom12.enggo_backend.dto.response.UserMinimalResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonPropertyOrder({
        "id", "themes", "skills", "content", "attachmentUrl", "difficulty",
        "explanation", "createdBy", "options", "createdAt", "updatedAt"
})
public class QuestionDetailResponse {
    private Integer id;
    private String content;
    private String explanation;
    Byte difficulty;
    String attachmentUrl;
    List<ThemeResponse> themes;
    List<SkillResponse> skills;
    private UserMinimalResponse createdBy;
    List<OptionResponse> options;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
