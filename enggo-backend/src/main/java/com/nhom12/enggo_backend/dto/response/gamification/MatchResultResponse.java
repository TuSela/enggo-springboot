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
    Integer player1Score;
    Integer player2Score;
    Integer eloChange;
    String status;
}
