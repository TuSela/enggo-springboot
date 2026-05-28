package com.nhom12.enggo_backend.service.social;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendService {

    FriendRepository friendRepository;
    FriendRequestRepository friendRequestRepository;
    UserRepository userRepository;

    @Transactional
    public void sendFriendRequest(Integer senderId, Integer receiverId) {
        if (senderId.equals(receiverId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED); // Bạn có thể map với một mã lỗi phù hợp hơn nếu có trong ErrorCode
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (friendRepository.existsByUsers(sender, receiver)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED); // Thay thế bằng mã lỗi Đã là bạn bè của bạn nếu có
        }

        // Bắt ngoại lệ nếu lời mời kết bạn đã tồn tại hoặc được gửi trước đó
        if (friendRequestRepository.existsBySenderAndReceiver(sender, receiver)) {
            throw new AppException(ErrorCode.FRIEND_REQUEST_EXISTED);
        }

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .build();
        
        friendRequestRepository.save(request);
    }

    @Transactional
    public void acceptFriendRequest(Integer requestId, Integer receiverId) {
        // Bắt ngoại lệ nếu tìm lời mời không thấy (chưa có lời mời nào được gửi hoặc sai ID)
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED); // Trả về lỗi không có quyền xác thực
        }

        Friend friend = Friend.builder()
                .user1(request.getSender())
                .user2(request.getReceiver())
                .build();

        friendRepository.save(friend);
        friendRequestRepository.delete(request);
    }

    @Transactional
    public void rejectFriendRequest(Integer requestId, Integer receiverId) {
        // Bắt ngoại lệ nếu tìm không thấy lời mời kết bạn để hủy/từ chối
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        friendRequestRepository.delete(request);
    }

    @Transactional
    public void unfriend(Integer currentUserId, Integer targetUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Friend friend = friendRepository.findByUsers(currentUser, targetUser)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND)); // Hoặc mã lỗi Chưa lập quan hệ bạn bè riêng của nhóm

        friendRepository.delete(friend);
    }
}