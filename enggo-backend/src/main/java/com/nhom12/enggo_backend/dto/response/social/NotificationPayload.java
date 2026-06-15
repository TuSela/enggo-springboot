package com.nhom12.enggo_backend.dto.response.social;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationPayload {
    String type;       // "FRIEND_REQUEST", "FRIEND_ACCEPTED", ...
    Integer fromUserId;
    String fromUsername;
    String message;
    Integer requestId; // ID của FriendRequest để accept/reject
}