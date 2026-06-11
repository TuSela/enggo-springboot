package com.nhom12.enggo_backend.dto.response.gamification;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchResultResponse {
    Integer matchId;
    Integer winnerId;
    PlayerResult player1;
    PlayerResult player2;
    String status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PlayerResult {
        String avatarUrl;
        Integer playerScore;
        Integer eloChange;
        Integer correctAnswersCount;
        Integer elo;
    }
}
