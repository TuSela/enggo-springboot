package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.QuizProgressRequest;
import com.nhom12.enggo_backend.dto.response.gamification.PvpMatchResponse;
import com.nhom12.enggo_backend.dto.response.gamification.QuizProgressResponse;
import com.nhom12.enggo_backend.service.gamification.MatchmakingService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
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
        PvpMatchResponse matchResponse = matchmakingService.findMatch(userId);

        if (matchResponse != null) {
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

    @MessageMapping("/match/{matchId}/progress")
    public void handleQuizProgress(
            @DestinationVariable Integer matchId,
            QuizProgressRequest request) {
        QuizProgressResponse broadcastData = matchmakingService.playing(request);
        messagingTemplate.convertAndSend("/topic/match/" + matchId + "/progress", broadcastData);
    }
    @MessageMapping("/match/{matchId}/submit")
    public void handleQuizSubmit(
            @DestinationVariable Integer matchId,
            QuizProgressRequest request) {
        QuizProgressResponse broadcastData = matchmakingService.playing(request);
        messagingTemplate.convertAndSend("/topic/match/" + matchId + "/progress", broadcastData);
    }
}