package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.gamification.BadgeEventRequest;
import com.nhom12.enggo_backend.dto.request.gamification.BadgeRequest;
import com.nhom12.enggo_backend.dto.request.gamification.EventRequest;
import com.nhom12.enggo_backend.dto.request.gamification.MissionBadgeRequest;
import com.nhom12.enggo_backend.dto.request.gamification.MissionProgressRequest;
import com.nhom12.enggo_backend.dto.request.gamification.MissionRequest;
import com.nhom12.enggo_backend.dto.request.gamification.PvpMatchRequest;
import com.nhom12.enggo_backend.dto.request.gamification.UserBadgeRequest;
import com.nhom12.enggo_backend.dto.response.gamification.BadgeEventResponse;
import com.nhom12.enggo_backend.dto.response.gamification.BadgeResponse;
import com.nhom12.enggo_backend.dto.response.gamification.EventResponse;
import com.nhom12.enggo_backend.dto.response.gamification.MissionBadgeResponse;
import com.nhom12.enggo_backend.dto.response.gamification.MissionProgressResponse;
import com.nhom12.enggo_backend.dto.response.gamification.MissionResponse;
import com.nhom12.enggo_backend.dto.response.gamification.PvpMatchResponse;
import com.nhom12.enggo_backend.dto.response.gamification.UserBadgeResponse;
import com.nhom12.enggo_backend.entity.exam.Exam;
import com.nhom12.enggo_backend.entity.exam.ExamAttempt;
import com.nhom12.enggo_backend.entity.gamification.Badge;
import com.nhom12.enggo_backend.entity.gamification.BadgeEvent;
import com.nhom12.enggo_backend.entity.gamification.BadgeEventId;
import com.nhom12.enggo_backend.entity.gamification.Event;
import com.nhom12.enggo_backend.entity.gamification.Mission;
import com.nhom12.enggo_backend.entity.gamification.MissionBadge;
import com.nhom12.enggo_backend.entity.gamification.MissionBadgeId;
import com.nhom12.enggo_backend.entity.gamification.MissionProgress;
import com.nhom12.enggo_backend.entity.gamification.PvpMatch;
import com.nhom12.enggo_backend.entity.gamification.UserBadge;
import com.nhom12.enggo_backend.entity.gamification.UserBadgeId;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.exam.ExamAttemptRepository;
import com.nhom12.enggo_backend.repository.exam.ExamRepository;
import com.nhom12.enggo_backend.repository.gamification.BadgeEventRepository;
import com.nhom12.enggo_backend.repository.gamification.BadgeRepository;
import com.nhom12.enggo_backend.repository.gamification.EventRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionBadgeRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionProgressRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionRepository;
import com.nhom12.enggo_backend.repository.gamification.PvpMatchRepository;
import com.nhom12.enggo_backend.repository.gamification.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GamificationService {
    private final BadgeRepository badgeRepository;
    private final EventRepository eventRepository;
    private final MissionRepository missionRepository;
    private final MissionProgressRepository missionProgressRepository;
    private final PvpMatchRepository pvpMatchRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeEventRepository badgeEventRepository;
    private final MissionBadgeRepository missionBadgeRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;

    @Transactional(readOnly = true)
    public List<BadgeResponse> getBadges() {
        return badgeRepository.findAll().stream().map(this::toBadgeResponse).toList();
    }

    @Transactional(readOnly = true)
    public BadgeResponse getBadge(Integer id) {
        return toBadgeResponse(findBadge(id));
    }

    public BadgeResponse createBadge(BadgeRequest request) {
        Badge badge = new Badge();
        applyBadgeRequest(badge, request);
        return toBadgeResponse(badgeRepository.save(badge));
    }

    public BadgeResponse updateBadge(Integer id, BadgeRequest request) {
        Badge badge = findBadge(id);
        applyBadgeRequest(badge, request);
        return toBadgeResponse(badgeRepository.save(badge));
    }

    public void deleteBadge(Integer id) {
        badgeRepository.delete(findBadge(id));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEvents() {
        return eventRepository.findAll().stream().map(this::toEventResponse).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(Integer id) {
        return toEventResponse(findEvent(id));
    }

    public EventResponse createEvent(EventRequest request) {
        Event event = new Event();
        applyEventRequest(event, request);
        return toEventResponse(eventRepository.save(event));
    }

    public EventResponse updateEvent(Integer id, EventRequest request) {
        Event event = findEvent(id);
        applyEventRequest(event, request);
        return toEventResponse(eventRepository.save(event));
    }

    public void deleteEvent(Integer id) {
        eventRepository.delete(findEvent(id));
    }

    @Transactional(readOnly = true)
    public List<MissionResponse> getMissions() {
        return missionRepository.findAll().stream().map(this::toMissionResponse).toList();
    }

    @Transactional(readOnly = true)
    public MissionResponse getMission(Integer id) {
        return toMissionResponse(findMission(id));
    }

    public MissionResponse createMission(MissionRequest request) {
        Mission mission = new Mission();
        applyMissionRequest(mission, request);
        return toMissionResponse(missionRepository.save(mission));
    }

    public MissionResponse updateMission(Integer id, MissionRequest request) {
        Mission mission = findMission(id);
        applyMissionRequest(mission, request);
        return toMissionResponse(missionRepository.save(mission));
    }

    public void deleteMission(Integer id) {
        missionRepository.delete(findMission(id));
    }

    @Transactional(readOnly = true)
    public List<MissionProgressResponse> getMissionProgresses() {
        return missionProgressRepository.findAll().stream().map(this::toMissionProgressResponse).toList();
    }

    @Transactional(readOnly = true)
    public MissionProgressResponse getMissionProgress(Integer id) {
        return toMissionProgressResponse(findMissionProgress(id));
    }

    public MissionProgressResponse createMissionProgress(MissionProgressRequest request) {
        MissionProgress progress = new MissionProgress();
        applyMissionProgressRequest(progress, request);
        return toMissionProgressResponse(missionProgressRepository.save(progress));
    }

    public MissionProgressResponse updateMissionProgress(Integer id, MissionProgressRequest request) {
        MissionProgress progress = findMissionProgress(id);
        applyMissionProgressRequest(progress, request);
        return toMissionProgressResponse(missionProgressRepository.save(progress));
    }

    public void deleteMissionProgress(Integer id) {
        missionProgressRepository.delete(findMissionProgress(id));
    }

    @Transactional(readOnly = true)
    public List<PvpMatchResponse> getPvpMatches() {
        return pvpMatchRepository.findAll().stream().map(this::toPvpMatchResponse).toList();
    }

    @Transactional(readOnly = true)
    public PvpMatchResponse getPvpMatch(Integer id) {
        return toPvpMatchResponse(findPvpMatch(id));
    }

    public PvpMatchResponse createPvpMatch(PvpMatchRequest request) {
        PvpMatch pvpMatch = new PvpMatch();
        applyPvpMatchRequest(pvpMatch, request);
        return toPvpMatchResponse(pvpMatchRepository.save(pvpMatch));
    }

    public PvpMatchResponse updatePvpMatch(Integer id, PvpMatchRequest request) {
        PvpMatch pvpMatch = findPvpMatch(id);
        applyPvpMatchRequest(pvpMatch, request);
        return toPvpMatchResponse(pvpMatchRepository.save(pvpMatch));
    }

    public void deletePvpMatch(Integer id) {
        pvpMatchRepository.delete(findPvpMatch(id));
    }

    @Transactional(readOnly = true)
    public List<UserBadgeResponse> getUserBadges() {
        return userBadgeRepository.findAll().stream().map(this::toUserBadgeResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserBadgeResponse getUserBadge(Integer userId, Integer badgeId) {
        return toUserBadgeResponse(findUserBadge(userId, badgeId));
    }

    public UserBadgeResponse createUserBadge(UserBadgeRequest request) {
        UserBadgeId id = new UserBadgeId(request.getUserId(), request.getBadgeId());
        if (userBadgeRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_EXISTED);
        }
        UserBadge userBadge = UserBadge.builder()
                .id(id)
                .user(findUser(request.getUserId()))
                .badge(findBadge(request.getBadgeId()))
                .build();
        return toUserBadgeResponse(userBadgeRepository.save(userBadge));
    }

    public UserBadgeResponse updateUserBadge(Integer userId, Integer badgeId, UserBadgeRequest request) {
        UserBadge userBadge = findUserBadge(userId, badgeId);
        if (!userId.equals(request.getUserId()) || !badgeId.equals(request.getBadgeId())) {
            userBadgeRepository.delete(userBadge);
            return createUserBadge(request);
        }
        return toUserBadgeResponse(userBadge);
    }

    public void deleteUserBadge(Integer userId, Integer badgeId) {
        userBadgeRepository.delete(findUserBadge(userId, badgeId));
    }

    @Transactional(readOnly = true)
    public List<BadgeEventResponse> getBadgeEvents() {
        return badgeEventRepository.findAll().stream().map(this::toBadgeEventResponse).toList();
    }

    @Transactional(readOnly = true)
    public BadgeEventResponse getBadgeEvent(Integer badgeId, Integer eventId) {
        return toBadgeEventResponse(findBadgeEvent(badgeId, eventId));
    }

    public BadgeEventResponse createBadgeEvent(BadgeEventRequest request) {
        BadgeEventId id = new BadgeEventId(request.getBadgeId(), request.getEventId());
        if (badgeEventRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_EXISTED);
        }
        BadgeEvent badgeEvent = BadgeEvent.builder()
                .id(id)
                .badge(findBadge(request.getBadgeId()))
                .event(findEvent(request.getEventId()))
                .minScoreRequired(request.getMinScoreRequired())
                .rewardExp(request.getRewardExp())
                .build();
        return toBadgeEventResponse(badgeEventRepository.save(badgeEvent));
    }

    public BadgeEventResponse updateBadgeEvent(Integer badgeId, Integer eventId, BadgeEventRequest request) {
        BadgeEvent badgeEvent = findBadgeEvent(badgeId, eventId);
        if (!badgeId.equals(request.getBadgeId()) || !eventId.equals(request.getEventId())) {
            badgeEventRepository.delete(badgeEvent);
            return createBadgeEvent(request);
        }
        badgeEvent.setMinScoreRequired(request.getMinScoreRequired());
        badgeEvent.setRewardExp(request.getRewardExp());
        return toBadgeEventResponse(badgeEventRepository.save(badgeEvent));
    }

    public void deleteBadgeEvent(Integer badgeId, Integer eventId) {
        badgeEventRepository.delete(findBadgeEvent(badgeId, eventId));
    }

    @Transactional(readOnly = true)
    public List<MissionBadgeResponse> getMissionBadges() {
        return missionBadgeRepository.findAll().stream().map(this::toMissionBadgeResponse).toList();
    }

    @Transactional(readOnly = true)
    public MissionBadgeResponse getMissionBadge(Integer missionId, Integer badgeId) {
        return toMissionBadgeResponse(findMissionBadge(missionId, badgeId));
    }

    public MissionBadgeResponse createMissionBadge(MissionBadgeRequest request) {
        MissionBadgeId id = new MissionBadgeId(request.getMissionId(), request.getBadgeId());
        if (missionBadgeRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_EXISTED);
        }
        MissionBadge missionBadge = MissionBadge.builder()
                .id(id)
                .mission(findMission(request.getMissionId()))
                .badge(findBadge(request.getBadgeId()))
                .bonusExp(request.getBonusExp())
                .build();
        return toMissionBadgeResponse(missionBadgeRepository.save(missionBadge));
    }

    public MissionBadgeResponse updateMissionBadge(Integer missionId, Integer badgeId, MissionBadgeRequest request) {
        MissionBadge missionBadge = findMissionBadge(missionId, badgeId);
        if (!missionId.equals(request.getMissionId()) || !badgeId.equals(request.getBadgeId())) {
            missionBadgeRepository.delete(missionBadge);
            return createMissionBadge(request);
        }
        missionBadge.setBonusExp(request.getBonusExp());
        return toMissionBadgeResponse(missionBadgeRepository.save(missionBadge));
    }

    public void deleteMissionBadge(Integer missionId, Integer badgeId) {
        missionBadgeRepository.delete(findMissionBadge(missionId, badgeId));
    }

    private void applyBadgeRequest(Badge badge, BadgeRequest request) {
        badge.setBadgeName(request.getBadgeName());
        badge.setDescription(request.getDescription());
        badge.setIconUrl(request.getIconUrl());
    }

    private void applyEventRequest(Event event, EventRequest request) {
        event.setEventName(request.getEventName());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setRequiredLevel(request.getRequiredLevel());
        event.setStatus(request.getStatus());
        event.setEventType(request.getEventType());
        event.setBannerUrl(request.getBannerUrl());
    }

    private void applyMissionRequest(Mission mission, MissionRequest request) {
        mission.setTitle(request.getTitle());
        mission.setDescription(request.getDescription());
        mission.setRewardExp(request.getRewardExp());
        mission.setMissionType(request.getMissionType());
        mission.setTargetValue(request.getTargetValue());
        mission.setMissionKey(request.getMissionKey());
        mission.setTimeLimitHours(request.getTimeLimitHours());
        mission.setStatus(request.getStatus());
    }

    private void applyMissionProgressRequest(MissionProgress progress, MissionProgressRequest request) {
        progress.setUser(findUser(request.getUserId()));
        progress.setMission(findMission(request.getMissionId()));
        progress.setCurrentValue(request.getCurrentValue());
        progress.setStatus(request.getStatus());
        progress.setDeadline(request.getDeadline());
    }

    private void applyPvpMatchRequest(PvpMatch pvpMatch, PvpMatchRequest request) {
        pvpMatch.setPlayer1(findUser(request.getPlayer1Id()));
        pvpMatch.setPlayer1Attempt(findExamAttemptOrNull(request.getPlayer1AttemptId()));
        pvpMatch.setPlayer2(findUser(request.getPlayer2Id()));
        pvpMatch.setPlayer2Attempt(findExamAttemptOrNull(request.getPlayer2AttemptId()));
        pvpMatch.setExam(findExam(request.getExamId()));
        pvpMatch.setPlayer1Score(request.getPlayer1Score());
        pvpMatch.setPlayer2Score(request.getPlayer2Score());
        pvpMatch.setWinner(findUserOrNull(request.getWinnerId()));
        pvpMatch.setStatus(request.getStatus());
        pvpMatch.setStartTime(request.getStartTime());
        pvpMatch.setEndTime(request.getEndTime());
    }

    private BadgeResponse toBadgeResponse(Badge badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .badgeName(badge.getBadgeName())
                .description(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .createdAt(badge.getCreatedAt())
                .build();
    }

    private EventResponse toEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .eventName(event.getEventName())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .requiredLevel(event.getRequiredLevel())
                .status(event.getStatus())
                .eventType(event.getEventType())
                .bannerUrl(event.getBannerUrl())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
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

    private MissionProgressResponse toMissionProgressResponse(MissionProgress progress) {
        return MissionProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .username(progress.getUser().getUsername())
                .missionId(progress.getMission().getId())
                .missionTitle(progress.getMission().getTitle())
                .currentValue(progress.getCurrentValue())
                .status(progress.getStatus())
                .deadline(progress.getDeadline())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }

    private PvpMatchResponse toPvpMatchResponse(PvpMatch pvpMatch) {
        User winner = pvpMatch.getWinner();
        ExamAttempt player1Attempt = pvpMatch.getPlayer1Attempt();
        ExamAttempt player2Attempt = pvpMatch.getPlayer2Attempt();
        return PvpMatchResponse.builder()
                .id(pvpMatch.getId())
                .player1Id(pvpMatch.getPlayer1().getId())
                .player1Username(pvpMatch.getPlayer1().getUsername())
                .player1AttemptId(player1Attempt != null ? player1Attempt.getId() : null)
                .player2Id(pvpMatch.getPlayer2().getId())
                .player2Username(pvpMatch.getPlayer2().getUsername())
                .player2AttemptId(player2Attempt != null ? player2Attempt.getId() : null)
                .examId(pvpMatch.getExam().getId())
                .examTitle(pvpMatch.getExam().getTitle())
                .player1Score(pvpMatch.getPlayer1Score())
                .player2Score(pvpMatch.getPlayer2Score())
                .winnerId(winner != null ? winner.getId() : null)
                .winnerUsername(winner != null ? winner.getUsername() : null)
                .status(pvpMatch.getStatus())
                .startTime(pvpMatch.getStartTime())
                .endTime(pvpMatch.getEndTime())
                .createdAt(pvpMatch.getCreatedAt())
                .build();
    }

    private UserBadgeResponse toUserBadgeResponse(UserBadge userBadge) {
        return UserBadgeResponse.builder()
                .userId(userBadge.getUser().getId())
                .username(userBadge.getUser().getUsername())
                .badgeId(userBadge.getBadge().getId())
                .badgeName(userBadge.getBadge().getBadgeName())
                .earnedAt(userBadge.getEarnedAt())
                .build();
    }

    private BadgeEventResponse toBadgeEventResponse(BadgeEvent badgeEvent) {
        return BadgeEventResponse.builder()
                .badgeId(badgeEvent.getBadge().getId())
                .badgeName(badgeEvent.getBadge().getBadgeName())
                .eventId(badgeEvent.getEvent().getId())
                .eventName(badgeEvent.getEvent().getEventName())
                .minScoreRequired(badgeEvent.getMinScoreRequired())
                .rewardExp(badgeEvent.getRewardExp())
                .build();
    }

    private MissionBadgeResponse toMissionBadgeResponse(MissionBadge missionBadge) {
        return MissionBadgeResponse.builder()
                .missionId(missionBadge.getMission().getId())
                .missionTitle(missionBadge.getMission().getTitle())
                .badgeId(missionBadge.getBadge().getId())
                .badgeName(missionBadge.getBadge().getBadgeName())
                .bonusExp(missionBadge.getBonusExp())
                .build();
    }

    private Badge findBadge(Integer id) {
        return badgeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private Event findEvent(Integer id) {
        return eventRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private Mission findMission(Integer id) {
        return missionRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private MissionProgress findMissionProgress(Integer id) {
        return missionProgressRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private PvpMatch findPvpMatch(Integer id) {
        return pvpMatchRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private UserBadge findUserBadge(Integer userId, Integer badgeId) {
        return userBadgeRepository.findById(new UserBadgeId(userId, badgeId))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private BadgeEvent findBadgeEvent(Integer badgeId, Integer eventId) {
        return badgeEventRepository.findById(new BadgeEventId(badgeId, eventId))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private MissionBadge findMissionBadge(Integer missionId, Integer badgeId) {
        return missionBadgeRepository.findById(new MissionBadgeId(missionId, badgeId))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private User findUser(Integer id) {
        return userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private User findUserOrNull(Integer id) {
        return id == null ? null : findUser(id);
    }

    private Exam findExam(Integer id) {
        return examRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private ExamAttempt findExamAttemptOrNull(Integer id) {
        return id == null ? null : examAttemptRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
