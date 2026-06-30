package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.exam.RandomBlueprintRequest;
import com.nhom12.enggo_backend.dto.response.gamification.PvpMatchResponse;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.exam.ThemeRepository;
import com.nhom12.enggo_backend.service.gamification.MatchmakingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/gamification/pvp/direct")
@RequiredArgsConstructor
public class DirectMatchController {

    private final MatchmakingService matchmakingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ThemeRepository themeRepository; // ← THÊM để lấy tên chủ đề

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Chuyển số độ khó → label tiếng Việt
    private String getDifficultyLabel(Byte difficulty) {
        if (difficulty == null) return "Vừa";
        return switch (difficulty.intValue()) {
            case 1 -> "Dễ";
            case 2 -> "Vừa";
            case 3 -> "Khó";
            default -> "Vừa";
        };
    }

    // Lấy tên chủ đề từ danh sách ID
    private String getThemeNames(List<Integer> themeIds) {
        if (themeIds == null || themeIds.isEmpty()) return "";
        return themeIds.stream()
                .map(id -> themeRepository.findById(id)
                        .map(t -> t.getThemeName())
                        .orElse("Chủ đề " + id))
                .collect(Collectors.joining(", "));
    }

    @PostMapping("/invite/{friendId}")
    public ApiResponse<PvpMatchResponse> inviteFriend(
            @PathVariable Integer friendId,
            @RequestBody RandomBlueprintRequest request) {

        User currentUser = getCurrentUser();

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        // Chặn mời PVP khi bạn đang offline: luồng mời trực tiếp này tạo
        // PvpMatch + sinh đề thi ngay lập tức và không có cơ chế timeout,
        // nên nếu người nhận offline thì match sẽ "treo" vô thời hạn.
        if (!"ONLINE".equalsIgnoreCase(friend.getStatus())) {
            throw new AppException(ErrorCode.FRIEND_OFFLINE);
        }

        PvpMatchResponse match = matchmakingService.createDirectMatch(
                currentUser.getId(), friendId, request);

        String themeNames = getThemeNames(request.getThemeIds());
        String difficultyLabel = getDifficultyLabel(request.getDifficulty());

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "PVP_INVITE");
        notification.put("requestId", match.getId());
        notification.put("fromUserId", currentUser.getId());
        notification.put("fromUsername", currentUser.getUsername());
        notification.put("message", currentUser.getUsername() + " mời bạn tham gia thách đấu!");
        notification.put("examTopic", themeNames);           // ← "Gia đình" thay vì "[4]"
        notification.put("difficulty", difficultyLabel);     // ← "Vừa" thay vì "2"
        notification.put("questionCount", request.getTotalQuestions());

        messagingTemplate.convertAndSendToUser(
                friend.getUsername(),
                "/queue/notifications",
                (Object) notification
        );

        return ApiResponse.<PvpMatchResponse>builder().result(match).build();
    }

    @PostMapping("/accept/{matchId}")
    public ApiResponse<Boolean> acceptInvite(@PathVariable Integer matchId) {
        User currentUser = getCurrentUser();
        String player1Username = matchmakingService.getPlayer1Username(matchId);

        Map<String, Object> eventForP1 = new HashMap<>();
        eventForP1.put("type", "PVP_ACCEPTED");
        eventForP1.put("matchId", matchId);
        eventForP1.put("fromUsername", currentUser.getUsername());
        eventForP1.put("message", currentUser.getUsername() + " đã chấp nhận lời mời!");

        Map<String, Object> eventForP2 = new HashMap<>();
        eventForP2.put("type", "PVP_ACCEPTED");
        eventForP2.put("matchId", matchId);
        eventForP2.put("message", "Lời mời đã được chấp nhận!");

        messagingTemplate.convertAndSendToUser(player1Username, "/queue/pvp", (Object) eventForP1);
        messagingTemplate.convertAndSendToUser(currentUser.getUsername(), "/queue/pvp", (Object) eventForP2);

        return ApiResponse.<Boolean>builder().result(true).build();
    }

    @PostMapping("/{matchId}/ready")
    public ApiResponse<Boolean> playerReady(@PathVariable Integer matchId) {
        User currentUser = getCurrentUser();
        String player1Username = matchmakingService.getPlayer1Username(matchId);

        Map<String, Object> event = new HashMap<>();
        event.put("type", "PVP_PLAYER_READY");
        event.put("matchId", matchId);
        event.put("username", currentUser.getUsername());
        event.put("message", currentUser.getUsername() + " đã sẵn sàng!");

        messagingTemplate.convertAndSendToUser(player1Username, "/queue/pvp", (Object) event);
        return ApiResponse.<Boolean>builder().result(true).build();
    }

    @PostMapping("/{matchId}/start")
    public ApiResponse<Boolean> startMatch(@PathVariable Integer matchId) {
        String player1Username = matchmakingService.getPlayer1Username(matchId);
        String player2Username = matchmakingService.getPlayer2Username(matchId);

        Map<String, Object> event = new HashMap<>();
        event.put("type", "PVP_START");
        event.put("matchId", matchId);
        event.put("message", "Trận đấu bắt đầu!");

        messagingTemplate.convertAndSendToUser(player1Username, "/queue/pvp", (Object) event);
        messagingTemplate.convertAndSendToUser(player2Username, "/queue/pvp", (Object) event);
        return ApiResponse.<Boolean>builder().result(true).build();
    }

    @DeleteMapping("/decline/{matchId}")
    public ApiResponse<Boolean> declineInvite(@PathVariable Integer matchId) {
        User currentUser = getCurrentUser();
        String player1Username = matchmakingService.getPlayer1Username(matchId);
        String player2Username = matchmakingService.getPlayer2Username(matchId);

        // Bug cũ: luôn gửi thông báo huỷ cho player1, kể cả khi chính player1
        // là người huỷ (lúc đó player2 không hề được báo, bị treo vô thời hạn
        // trong WaitingRoomActivity). Giờ xác định đúng người NHẬN thông báo
        // là người còn lại, không phải người vừa bấm huỷ.
        String recipientUsername = currentUser.getUsername().equals(player1Username)
                ? player2Username
                : player1Username;

        Map<String, Object> event = new HashMap<>();
        event.put("type", "PVP_DECLINED");
        event.put("matchId", matchId);
        event.put("fromUserId", currentUser.getId());
        event.put("message", currentUser.getUsername() + " đã từ chối lời mời");

        messagingTemplate.convertAndSendToUser(recipientUsername, "/queue/pvp", (Object) event);
        return ApiResponse.<Boolean>builder().result(true).build();
    }
}