package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.QuizProgressRequest;
import com.nhom12.enggo_backend.dto.response.gamification.PvpMatchResponse;
import com.nhom12.enggo_backend.dto.response.gamification.QuizProgressResponse;
import com.nhom12.enggo_backend.entity.exam.Exam;
import com.nhom12.enggo_backend.entity.gamification.PvpMatch;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.exam.ExamRepository;
import com.nhom12.enggo_backend.repository.gamification.PvpMatchRepository;
import com.nhom12.enggo_backend.repository.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MatchmakingService {

    private final StringRedisTemplate redisTemplate;
    private final PvpMatchRepository pvpMatchRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;

    private static final String MATCH_QUEUE_KEY = "pvp:match:queue";

    public MatchmakingService(StringRedisTemplate redisTemplate,
                              PvpMatchRepository pvpMatchRepository,
                              UserRepository userRepository,
                              ExamRepository examRepository) {
        this.redisTemplate = redisTemplate;
        this.pvpMatchRepository = pvpMatchRepository;
        this.userRepository = userRepository;
        this.examRepository = examRepository;
    }

    @Transactional
    public synchronized PvpMatchResponse findMatch(Integer userId) {
        String userIdStr = String.valueOf(userId);

        Boolean isWaiting = redisTemplate.opsForSet().isMember(MATCH_QUEUE_KEY, userIdStr);
        if (Boolean.TRUE.equals(isWaiting)) {
            return null;
        }

        String opponentIdStr = redisTemplate.opsForSet().pop(MATCH_QUEUE_KEY);

        if (opponentIdStr == null) {
            redisTemplate.opsForSet().add(MATCH_QUEUE_KEY, userIdStr);
            return null;
        }

        Integer opponentId = Integer.parseInt(opponentIdStr);

        User player1 = userRepository.findById(opponentId)
                .orElseThrow(() -> new RuntimeException("Player 1 not found"));
        User player2 = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Player 2 not found"));

        Exam randomExam = examRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No exam available for PVP match"));

        PvpMatch newMatch = PvpMatch.builder()
                .player1(player1)
                .player2(player2)
                .exam(randomExam)
                .status("PLAYING")
                .startTime(LocalDateTime.now())
                .build();


        PvpMatch savedMatch = pvpMatchRepository.save(newMatch);

        return PvpMatchResponse.builder()
                .id(savedMatch.getId())
                .player1Id(savedMatch.getPlayer1().getId())
                .player1Username(savedMatch.getPlayer1().getUsername())
                .player2Id(savedMatch.getPlayer2().getId())
                .player2Username(savedMatch.getPlayer2().getUsername())
                .examId(savedMatch.getExam().getId())
                .examTitle(savedMatch.getExam().getTitle())
                .status(savedMatch.getStatus())
                .startTime(savedMatch.getStartTime())
                .build();
    }

    public void cancelFindMatch(Integer userId) {
        redisTemplate.opsForSet().remove(MATCH_QUEUE_KEY, String.valueOf(userId));
    }

    public QuizProgressResponse playing(QuizProgressRequest request){
    QuizProgressResponse broadcastData = QuizProgressResponse.builder()
            .playerId(request.getPlayerId())
            .currentQuestionIndex(request.getCurrentQuestionIndex())
            .build();
        return broadcastData;
    }
}