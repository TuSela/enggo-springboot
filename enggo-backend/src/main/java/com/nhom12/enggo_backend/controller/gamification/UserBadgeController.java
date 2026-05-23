package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.gamification.UserBadgeRequest;
import com.nhom12.enggo_backend.dto.response.gamification.UserBadgeResponse;
import com.nhom12.enggo_backend.service.gamification.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gamification/user-badges")
@RequiredArgsConstructor
public class UserBadgeController {
    private final GamificationService gamificationService;

    @GetMapping
    ApiResponse<List<UserBadgeResponse>> getUserBadges() {
        return ApiResponse.<List<UserBadgeResponse>>builder().result(gamificationService.getUserBadges()).build();
    }

    @GetMapping("/{userId}/{badgeId}")
    ApiResponse<UserBadgeResponse> getUserBadge(@PathVariable Integer userId, @PathVariable Integer badgeId) {
        return ApiResponse.<UserBadgeResponse>builder().result(gamificationService.getUserBadge(userId, badgeId)).build();
    }

    @PostMapping
    ApiResponse<UserBadgeResponse> createUserBadge(@RequestBody UserBadgeRequest request) {
        return ApiResponse.<UserBadgeResponse>builder().result(gamificationService.createUserBadge(request)).build();
    }

    @PutMapping("/{userId}/{badgeId}")
    ApiResponse<UserBadgeResponse> updateUserBadge(
            @PathVariable Integer userId,
            @PathVariable Integer badgeId,
            @RequestBody UserBadgeRequest request
    ) {
        return ApiResponse.<UserBadgeResponse>builder()
                .result(gamificationService.updateUserBadge(userId, badgeId, request))
                .build();
    }

    @DeleteMapping("/{userId}/{badgeId}")
    ApiResponse<String> deleteUserBadge(@PathVariable Integer userId, @PathVariable Integer badgeId) {
        gamificationService.deleteUserBadge(userId, badgeId);
        return ApiResponse.<String>builder().result("User badge has been deleted").build();
    }
}
