package com.nhom12.enggo_backend.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String email;
    String password;
    String avatarUrl;
    String bio;
    String status;
    List<String> roles;
    Integer rankPoint;
}
