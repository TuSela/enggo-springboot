package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.gamification.BadgeEventRequest;
import com.nhom12.enggo_backend.dto.response.gamification.BadgeEventResponse;
import com.nhom12.enggo_backend.service.gamification.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gamification/badge-events")
@RequiredArgsConstructor
public class BadgeEventController {
    private final GamificationService gamificationService;

    @GetMapping
    ApiResponse<List<BadgeEventResponse>> getBadgeEvents() {
        return ApiResponse.<List<BadgeEventResponse>>builder().result(gamificationService.getBadgeEvents()).build();
    }

    @GetMapping("/{badgeId}/{eventId}")
    ApiResponse<BadgeEventResponse> getBadgeEvent(@PathVariable Integer badgeId, @PathVariable Integer eventId) {
        return ApiResponse.<BadgeEventResponse>builder().result(gamificationService.getBadgeEvent(badgeId, eventId)).build();
    }

    @PostMapping
    ApiResponse<BadgeEventResponse> createBadgeEvent(@RequestBody BadgeEventRequest request) {
        return ApiResponse.<BadgeEventResponse>builder().result(gamificationService.createBadgeEvent(request)).build();
    }

    @PutMapping("/{badgeId}/{eventId}")
    ApiResponse<BadgeEventResponse> updateBadgeEvent(
            @PathVariable Integer badgeId,
            @PathVariable Integer eventId,
            @RequestBody BadgeEventRequest request
    ) {
        return ApiResponse.<BadgeEventResponse>builder()
                .result(gamificationService.updateBadgeEvent(badgeId, eventId, request))
                .build();
    }

    @DeleteMapping("/{badgeId}/{eventId}")
    ApiResponse<String> deleteBadgeEvent(@PathVariable Integer badgeId, @PathVariable Integer eventId) {
        gamificationService.deleteBadgeEvent(badgeId, eventId);
        return ApiResponse.<String>builder().result("Badge event has been deleted").build();
    }
}
