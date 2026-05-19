package com.nhom12.enggo_backend.dto.response.exam;

import com.nhom12.enggo_backend.entity.exam.Exam;
import com.nhom12.enggo_backend.entity.identity.User;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExamSubmitResponse {
    Integer attemptId;
    Integer examId;
    BigDecimal totalScore;
    Integer correctAnswersCount;
    Integer totalQuestions;
    LocalDateTime startedAt;
    LocalDateTime completedAt;
    Long timeTakenSeconds;
}
