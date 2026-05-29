package com.nhom12.enggo_backend.dto.response.gamification;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class QuizProgressResponse {
    private Integer playerId;           // Ai là người vừa trả lời
    private Integer currentQuestionIndex;
}
