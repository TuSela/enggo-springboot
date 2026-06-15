package com.nhom12.enggo_backend.controller.social;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.response.social.FriendResponse;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.service.social.FriendListService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/social/friends")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendListController {

    FriendListService friendListService;
    UserRepository userRepository;

    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return user.getId();
    }

    // 1. Lấy toàn bộ danh sách bạn bè
    // GET /social/friends
    @GetMapping()
    public ApiResponse<List<FriendResponse>> getAllFriends() {
        return ApiResponse.<List<FriendResponse>>builder()
                .result(friendListService.getAllFriends(getCurrentUserId()))
                .build();
    }

    // 2. Lấy bạn bè đang online (phần "ĐANG HOẠT ĐỘNG")
    // GET /social/friends/online
    @GetMapping("/online")
    public ApiResponse<List<FriendResponse>> getOnlineFriends() {
        return ApiResponse.<List<FriendResponse>>builder()
                .result(friendListService.getOnlineFriends(getCurrentUserId()))
                .build();
    }

    // 3. Tìm kiếm bạn bè theo tên
    // GET /social/friends/search?keyword=linh
    @GetMapping("/search")
    public ApiResponse<List<FriendResponse>> searchFriends(
            @RequestParam String keyword) {
        return ApiResponse.<List<FriendResponse>>builder()
                .result(friendListService.searchFriends(getCurrentUserId(), keyword))
                .build();
    }
}