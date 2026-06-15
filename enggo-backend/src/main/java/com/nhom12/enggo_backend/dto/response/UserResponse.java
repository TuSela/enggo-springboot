package com.nhom12.enggo_backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Integer id;
    String username;
    String email;
    Integer exp;
    Integer level;
    Integer streakDays;
    Integer completedTasks;
    Integer pvpWins;
    String avatarUrl;
    String status;
    String bio;
    Integer elo;
    LocalDateTime createdAt;
    Set<RoleResponse> roles;
}
