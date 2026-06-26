package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.gamification.MissionProgressRequest;
import com.nhom12.enggo_backend.dto.response.gamification.MissionProgressResponse;
import com.nhom12.enggo_backend.dto.response.gamification.MissionResponse;
import com.nhom12.enggo_backend.dto.response.gamification.ClaimRewardResponse;
import com.nhom12.enggo_backend.entity.gamification.Mission;
import com.nhom12.enggo_backend.entity.gamification.MissionProgress;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionProgressRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service handling daily missions for a specific user.
 * Provides methods to fetch today's missions, update progress and claim rewards.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserMissionService {

    private final MissionRepository missionRepository;
    private final MissionProgressRepository missionProgressRepository;
    private final UserRepository userRepository;

    /**
     * Retrieve today's missions with progress for the given user.
     */
    @Transactional(readOnly = true)
    public List<MissionProgressResponse> getTodayMissions(Integer userId) {
        User user = findUser(userId);
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusDays(1);
        List<MissionProgress> progresses = missionProgressRepository
                .findByUserIdAndDeadlineBetween(user.getId(), start, end);
        return progresses.stream().map(this::toProgressResponse).collect(Collectors.toList());
    }
    private MissionResponse toMissionResponse(Mission mission) {
        return MissionResponse.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .rewardExp(mission.getRewardExp())
                .missionType(mission.getMissionType())
                .targetValue(mission.getTargetValue())
                .missionKey(mission.getMissionKey())
                .timeLimitHours(mission.getTimeLimitHours())
                .status(mission.getStatus())
                .createdAt(mission.getCreatedAt())
                .build();
    }
    /**
     * Increment progress for a mission. If the target is reached, status becomes COMPLETED.
     */
    @Transactional
    public MissionProgressResponse incrementProgress(Integer userId, Integer missionId, int increment) {
        MissionProgress progress = findProgress(userId, missionId);
        if ("CLAIMED".equals(progress.getStatus())) {
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }
        int newValue = (progress.getCurrentValue() == null ? 0 : progress.getCurrentValue()) + increment;
        progress.setCurrentValue(newValue);
        // If target reached, mark as COMPLETED
        if (progress.getMission().getTargetValue() != null && newValue >= progress.getMission().getTargetValue()) {
            progress.setStatus("COMPLETED");
        }
        MissionProgress saved = missionProgressRepository.save(progress);
        return toProgressResponse(saved);
    }

    /**
     * Claim reward for a completed mission.
     */
    @Transactional
    public ClaimRewardResponse claimReward(Integer userId, Integer missionId) {
        MissionProgress progress = findProgress(userId, missionId);

        if (progress.getMission().getTargetValue() > progress.getCurrentValue()) {
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }
        // Ensure not already claimed
        if ("CLAIMED".equals(progress.getStatus())) {
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }
        // Add experience to user
        User user = progress.getUser();
        int reward = progress.getMission().getRewardExp() != null ? progress.getMission().getRewardExp() : 0;
        user.addExp(reward);
        userRepository.save(user);
        // Update progress status
        progress.setStatus("CLAIMED");
        missionProgressRepository.save(progress);
        return ClaimRewardResponse.builder()
                .expAwarded(reward)
                .newTotalExp(user.getExp())
                .status("CLAIMED")
                .build();
    }

    // Helper methods -----------------------------------------------------
    private User findUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private MissionProgress findProgress(Integer userId, Integer missionId) {
        return missionProgressRepository.findByUserIdAndMissionId(userId, missionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private MissionProgressResponse toProgressResponse(MissionProgress progress) {
        return MissionProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .username(progress.getUser().getUsername())
                .missionResponse(toMissionResponse(progress.getMission()))
                .currentValue(progress.getCurrentValue())
                .status(progress.getStatus())
                .deadline(progress.getDeadline())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }
}
