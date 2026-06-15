package com.nhom12.enggo_backend.service.social;

import com.nhom12.enggo_backend.dto.response.social.NotificationPayload;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.entity.social.Friend;
import com.nhom12.enggo_backend.entity.social.FriendRequest;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.social.FriendRepository;
import com.nhom12.enggo_backend.repository.social.FriendRequestRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendService {

    FriendRepository friendRepository;
    FriendRequestRepository friendRequestRepository;
    UserRepository userRepository;
    SimpMessagingTemplate messagingTemplate; // ← THÊM để gửi WebSocket

    @Transactional
    public void sendFriendRequest(Integer senderId, Integer receiverId) {
        if (senderId.equals(receiverId))
            throw new AppException(ErrorCode.USER_NOT_EXISTED);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (friendRepository.existsByUsers(sender, receiver))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (friendRequestRepository.existsBySenderAndReceiver(sender, receiver))
            throw new AppException(ErrorCode.FRIEND_REQUEST_EXISTED);

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .build();
        friendRequestRepository.save(request);

        // Gửi thông báo realtime tới người nhận
        messagingTemplate.convertAndSendToUser(
                receiver.getUsername(),          // username của người nhận
                "/queue/notifications",          // destination
                NotificationPayload.builder()
                        .type("FRIEND_REQUEST")
                        .fromUserId(sender.getId())
                        .fromUsername(sender.getUsername())
                        .requestId(request.getId())
                        .message(sender.getUsername() + " đã gửi lời mời kết bạn cho bạn")
                        .build()
        );
    }

    @Transactional
    public void acceptFriendRequest(Integer requestId, Integer receiverId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!request.getReceiver().getId().equals(receiverId))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        Friend friend = Friend.builder()
                .user1(request.getSender())
                .user2(request.getReceiver())
                .build();
        friendRepository.save(friend);
        friendRequestRepository.delete(request);

        // Thông báo cho người gửi biết đã được chấp nhận
        messagingTemplate.convertAndSendToUser(
                request.getSender().getUsername(),
                "/queue/notifications",
                NotificationPayload.builder()
                        .type("FRIEND_ACCEPTED")
                        .fromUserId(request.getReceiver().getId())
                        .fromUsername(request.getReceiver().getUsername())
                        .message(request.getReceiver().getUsername() + " đã chấp nhận lời mời kết bạn")
                        .build()
        );
    }

    @Transactional
    public void rejectFriendRequest(Integer requestId, Integer receiverId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!request.getReceiver().getId().equals(receiverId))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        friendRequestRepository.delete(request);
    }

    @Transactional
    public void unfriend(Integer currentUserId, Integer targetUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Friend friend = friendRepository.findByUsers(currentUser, targetUser)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        friendRepository.delete(friend);
    }
}