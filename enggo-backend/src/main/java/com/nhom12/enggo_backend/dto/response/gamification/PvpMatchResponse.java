package com.nhom12.enggo_backend.dto.response.gamification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PvpMatchResponse {
    Integer id;
    Integer player1Id;
    String player1Username;
    Integer player1AttemptId;
    Integer player2Id;
    String player2Username;
    Integer player2AttemptId;
    Integer examId;
    String examTitle;
    Integer player1Score;
    Integer player2Score;
    Integer winnerId;
    String winnerUsername;
    String status;
    LocalDateTime startTime;
    LocalDateTime endTime;
    LocalDateTime createdAt;
}
