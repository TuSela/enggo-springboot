package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.response.gamification.PvpMatchResponse;
import com.nhom12.enggo_backend.service.gamification.MatchmakingService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class MatchController {

    private final MatchmakingService matchmakingService;
    private final SimpMessagingTemplate messagingTemplate;

    public MatchController(MatchmakingService matchmakingService, SimpMessagingTemplate messagingTemplate) {
        this.matchmakingService = matchmakingService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/join-queue")
    public void joinQueue(@Payload Integer userId) {
        // Service trả về thẳng DTO an toàn
        PvpMatchResponse matchResponse = matchmakingService.findMatch(userId);

        if (matchResponse != null) {
            // Bắn dữ liệu về cho cả 2 người chơi dựa vào ID lấy từ DTO
            messagingTemplate.convertAndSend("/topic/match/" + matchResponse.getPlayer1Id(), matchResponse);
            messagingTemplate.convertAndSend("/topic/match/" + matchResponse.getPlayer2Id(), matchResponse);
        } else {
            messagingTemplate.convertAndSend("/topic/queue-status/" + userId, "WAITING");
        }
    }

    @MessageMapping("/leave-queue")
    public void leaveQueue(@Payload Integer userId) {
        matchmakingService.cancelFindMatch(userId);
        messagingTemplate.convertAndSend("/topic/queue-status/" + userId, "CANCELLED");
    }
}