package com.nhom12.enggo_backend.dto.response.gamification;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PvpMatchResponse {
    Integer id;

    Integer player1Id;
    String avatarUrlP1;
    String player1Username;
    Integer eloP1;
    Integer player1AttemptId;

    Integer player2Id;
    String avatarUrlP2;
    String player2Username;
    Integer eloP2;
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
