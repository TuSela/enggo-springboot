package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.gamification.MissionRequest;
import com.nhom12.enggo_backend.dto.response.gamification.MissionResponse;
import com.nhom12.enggo_backend.service.gamification.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gamification/missions")
@RequiredArgsConstructor
public class MissionController {
    private final GamificationService gamificationService;

    @GetMapping
    ApiResponse<List<MissionResponse>> getMissions() {
        return ApiResponse.<List<MissionResponse>>builder().result(gamificationService.getMissions()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<MissionResponse> getMission(@PathVariable Integer id) {
        return ApiResponse.<MissionResponse>builder().result(gamificationService.getMission(id)).build();
    }

    @PostMapping
    ApiResponse<MissionResponse> createMission(@RequestBody MissionRequest request) {
        return ApiResponse.<MissionResponse>builder().result(gamificationService.createMission(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<MissionResponse> updateMission(@PathVariable Integer id, @RequestBody MissionRequest request) {
        return ApiResponse.<MissionResponse>builder().result(gamificationService.updateMission(id, request)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteMission(@PathVariable Integer id) {
        gamificationService.deleteMission(id);
        return ApiResponse.<String>builder().result("Mission has been deleted").build();
    }
}
