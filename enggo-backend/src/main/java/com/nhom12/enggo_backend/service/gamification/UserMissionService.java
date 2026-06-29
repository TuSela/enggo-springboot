package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.response.gamification.*;
import com.nhom12.enggo_backend.entity.gamification.*;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.gamification.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserMissionService {

    private final MissionRepository missionRepository;
    private final MissionProgressRepository missionProgressRepository;
    private final UserRepository userRepository;

    private final BadgeRepository badgeRepository;
    private final MissionBadgeRepository missionBadgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final LevelService levelService;

    // 2. INJECT GAME PROPERTIES VÀO SERVICE
    private final LevelProperties gameProperties;

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

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void incrementProgressSafely(Integer userId, Integer missionId, int increment) {
        try {
            incrementProgress(userId, missionId, increment);
        } catch (Exception e) {
            log.warn("Failed to increment mission {} for user {}: {}", missionId, userId, e.getMessage());
        }
    }

    @Transactional
    public MissionProgressResponse incrementProgress(Integer userId, Integer missionId, int increment) {
        MissionProgress progress = findProgress(userId, missionId);
        if ("CLAIMED".equals(progress.getStatus())) {
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }
        int newValue = (progress.getCurrentValue() == null ? 0 : progress.getCurrentValue()) + increment;
        progress.setCurrentValue(newValue);
        if (progress.getMission().getTargetValue() != null && newValue >= progress.getMission().getTargetValue()) {
            progress.setStatus("COMPLETED");
        }
        MissionProgress saved = missionProgressRepository.save(progress);
        return toProgressResponse(saved);
    }

    @Transactional
    public ClaimRewardResponse claimReward(Integer userId, Integer missionId) {
        MissionProgress progress = findProgress(userId, missionId);

        if (progress.getMission().getTargetValue() > progress.getCurrentValue()) {
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }

        if ("CLAIMED".equals(progress.getStatus())) {
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }

        // 3. XỬ LÝ CỘNG EXP VÀ TÍNH TOÁN LEVEL TỪ FILE APPLICATION.YML
        User user = progress.getUser();
        int reward = progress.getMission().getRewardExp() != null ? progress.getMission().getRewardExp() : 0;

        // Cộng exp trước
        user.addExp(reward);
        int totalExpAfterReward = user.getExp();

        // Tìm level phù hợp nhất từ cấu hình (Tìm level cao nhất mà reqExp <= totalExpAfterReward)
        int calculatedLevel = gameProperties.getLevels().stream()
                .filter(lvlConfig -> totalExpAfterReward >= lvlConfig.getReqExp())
                .mapToInt(LevelProperties.LevelConfig::getLevel)
                .max()
                .orElse(1); // Mặc định là level 1 nếu xảy ra lỗi cấu hình

        user.setLevel(calculatedLevel); // Gán level mới cho user
        userRepository.save(user);

        // 4. Cập nhật trạng thái tiến độ thành CLAIMED
        progress.setStatus("CLAIMED");
        missionProgressRepository.save(progress);

        Mission mission = progress.getMission();
        List<MissionBadge> missionBadges = missionBadgeRepository.findByMission(mission);
        List<Badge> earnedBadges = new ArrayList<>();

        if (missionBadges != null && !missionBadges.isEmpty()) {
            for (MissionBadge mb : missionBadges) {
                Badge badge = mb.getBadge();
                if (badge != null) {
                    UserBadgeId userBadgeId = new UserBadgeId(user.getId(), badge.getId());
                    boolean isAlreadyOwned = userBadgeRepository.existsById(userBadgeId);

                    if (!isAlreadyOwned) {
                        UserBadge userBadge = UserBadge.builder()
                                .id(userBadgeId)
                                .user(user)
                                .badge(badge)
                                .build();

                        userBadgeRepository.save(userBadge);
                        earnedBadges.add(badge);
                    }
                }
            }
        }

        List<BadgeResponse> badgeResponseList = earnedBadges.stream()
                .map(badge -> BadgeResponse.builder()
                        .id(badge.getId())
                        .badgeName(badge.getBadgeName())
                        .description(badge.getDescription())
                        .iconUrl(badge.getIconUrl())
                        .build())
                .toList();

        return ClaimRewardResponse.builder()
                .expAwarded(reward)
                .newTotalExp(user.getExp())
                .status("CLAIMED")
                .badgeResponse(badgeResponseList)
                .build();
    }

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