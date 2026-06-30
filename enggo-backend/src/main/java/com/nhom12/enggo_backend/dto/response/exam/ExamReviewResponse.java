package com.nhom12.enggo_backend.dto.response.exam;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response chuyên dụng cho màn hình "Xem lại bài làm" ngay sau khi nộp bài
 * hoặc khi người dùng xem lại lịch sử làm bài.
 *
 * Khác với ExamSubmitResponse (dùng để trả kết quả ngay lúc submit, có kèm levelInfo),
 * response này tập trung vào việc tổng hợp số liệu thống kê + chi tiết từng câu
 * để FE render trực tiếp màn hình review (đúng/sai từng câu, đáp án đúng, giải thích...).
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamReviewResponse {
    // ---- Thông tin chung của lượt làm bài ----
    Integer attemptId;
    Integer examId;
    String examTitle;
    Byte difficulty;

    // ---- Kết quả tổng quan ----
    BigDecimal totalScore;          // điểm trên thang 10
    Integer totalQuestions;
    Integer correctAnswersCount;
    Integer wrongAnswersCount;
    BigDecimal accuracyPercent;     // % số câu đúng, làm tròn 2 chữ số

    // ---- Thời gian ----
    LocalDateTime startedAt;
    LocalDateTime completedAt;
    String timeSpent;               // định dạng "mm:ss"

    // ---- Thưởng exp ----
    Integer expGained;
    Integer bonusExp;
    Integer totalExpGained;

    // ---- Chi tiết từng câu, đã sắp theo thứ tự xuất hiện trong đề ----
    List<ExamAttemptDetailResponse> questions;
}