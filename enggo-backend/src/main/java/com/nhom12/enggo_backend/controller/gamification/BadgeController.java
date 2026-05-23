package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.gamification.BadgeRequest;
import com.nhom12.enggo_backend.dto.response.gamification.BadgeResponse;
import com.nhom12.enggo_backend.service.gamification.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/gamification/badges")
@RequiredArgsConstructor
public class BadgeController {
    private final GamificationService gamificationService;

    @GetMapping
    ApiResponse<List<BadgeResponse>> getBadges() {
        return ApiResponse.<List<BadgeResponse>>builder().result(gamificationService.getBadges()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<BadgeResponse> getBadge(@PathVariable Integer id) {
        return ApiResponse.<BadgeResponse>builder().result(gamificationService.getBadge(id)).build();
    }

    @PostMapping
    ApiResponse<BadgeResponse> createBadge(@RequestBody BadgeRequest request) {
        return ApiResponse.<BadgeResponse>builder().result(gamificationService.createBadge(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<BadgeResponse> updateBadge(@PathVariable Integer id, @RequestBody BadgeRequest request) {
        return ApiResponse.<BadgeResponse>builder().result(gamificationService.updateBadge(id, request)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteBadge(@PathVariable Integer id) {
        gamificationService.deleteBadge(id);
        return ApiResponse.<String>builder().result("Badge has been deleted").build();
    }
}
