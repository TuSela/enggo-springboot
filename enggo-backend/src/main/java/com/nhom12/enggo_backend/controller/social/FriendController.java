package com.nhom12.enggo_backend.controller.social;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.service.social.FriendService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/social/friends")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendController {

    FriendService friendService;
    UserRepository userRepository;

    /**
     * Hàm helper trích xuất username từ Token và tìm ID tương ứng từ Database
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName(); // Lấy username từ thuộc tính .subject() của JWT
            
            // Tìm thực thể User dựa vào username để lấy ra ID nguyên bản
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài khoản người dùng"));
            return user.getId();
        }
        throw new RuntimeException("Tài khoản chưa được xác thực hoặc phiên đăng nhập hết hạn");
    }

    // 1. API Gửi lời mời kết bạn
    @PostMapping("/request/{receiverId}")
    public ApiResponse<Boolean> sendFriendRequest(@PathVariable Integer receiverId) {
        Integer currentUserId = getCurrentUserId(); 
        friendService.sendFriendRequest(currentUserId, receiverId);
        return ApiResponse.<Boolean>builder()
                .result(true)
                .build();
    }

    // 2. API Chấp nhận lời mời kết bạn
    @PostMapping("/accept/{requestId}")
    public ApiResponse<Boolean> acceptFriendRequest(@PathVariable Integer requestId) {
        Integer currentUserId = getCurrentUserId(); 
        friendService.acceptFriendRequest(requestId, currentUserId);
        return ApiResponse.<Boolean>builder()
                .result(true)
                .build();
    }

    // 3. API Từ chối lời mời kết bạn
    @DeleteMapping("/reject/{requestId}")
    public ApiResponse<Boolean> rejectFriendRequest(@PathVariable Integer requestId) {
        Integer currentUserId = getCurrentUserId(); 
        friendService.rejectFriendRequest(requestId, currentUserId);
        return ApiResponse.<Boolean>builder()
                .result(true)
                .build();
    }

    // 4. API Hủy kết bạn
    @DeleteMapping("/unfriend/{targetUserId}")
    public ApiResponse<Boolean> unfriend(@PathVariable Integer targetUserId) {
        Integer currentUserId = getCurrentUserId(); 
        friendService.unfriend(currentUserId, targetUserId);
        return ApiResponse.<Boolean>builder()
                .result(true)
                .build();
    }
}