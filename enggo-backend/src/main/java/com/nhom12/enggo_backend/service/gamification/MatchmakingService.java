package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.QuizProgressRequest;
import com.nhom12.enggo_backend.dto.request.exam.ExamAnswerRequest;
import com.nhom12.enggo_backend.dto.request.exam.ExamSubmitRequest;
import com.nhom12.enggo_backend.dto.response.exam.ExamDisplayResponse;
import com.nhom12.enggo_backend.dto.response.gamification.ExamPvpDisplayResponse;
import com.nhom12.enggo_backend.dto.response.gamification.PvpMatchResponse;
import com.nhom12.enggo_backend.dto.response.gamification.QuizProgressResponse;
import com.nhom12.enggo_backend.entity.exam.Exam;
import com.nhom12.enggo_backend.entity.exam.ExamAttempt;
import com.nhom12.enggo_backend.entity.gamification.PvpMatch;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.mapper.exam.ExamMapper;
import com.nhom12.enggo_backend.repository.exam.ExamAttemptRepository;
import com.nhom12.enggo_backend.repository.exam.ExamRepository;
import com.nhom12.enggo_backend.repository.exam.QuestionRepository;
import com.nhom12.enggo_backend.repository.gamification.PvpMatchRepository;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.service.exam.ExamAttemptService;
import com.nhom12.enggo_backend.service.exam.ExamService;
import com.nhom12.enggo_backend.service.exam.ScoreCheck;
import org.hibernate.type.descriptor.java.ObjectJavaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

@Service
public class MatchmakingService {

    private final StringRedisTemplate redisTemplate;
    private final PvpMatchRepository pvpMatchRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;

    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private ExamAttemptRepository examAttemptRepository;


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
    public synchronized PvpMatchResponse findMatch(User player2) {
        String userIdStr = String.valueOf(player2.getId());
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


        Exam randomExam = examRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No exam available for PVP match"));

        PvpMatch newMatch = PvpMatch.builder()
                .player1(player1)
                .player2(player2)
                .exam(randomExam)
                .status(String.valueOf(MatchStatus.PLAYING))
                .player1Score(0)
                .player2Score(0)
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
    @Autowired
    ExamAttemptService examAttemptService;
    @Autowired
    QuestionRepository questionRepository;

    @Transactional
    public QuizProgressResponse playing(Integer matchId, ExamAnswerRequest request, Principal principal){
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        System.out.println("Id tran dau: " + matchId);
        PvpMatch pvpMatch = pvpMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match Not Found"));

        var question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question Not Found"));

        // Ki?m tra c�u h?i c� trong d? thi c?a tr?n d?u kh�ng
        boolean isBelong = pvpMatch.getExam().getExamQuestions().stream()
                .anyMatch(e -> e.getQuestion().getId().equals(question.getId()));
        if (!isBelong) {
            throw new RuntimeException("Question Not Found in Exam");
        }

        ScoreCheck result = examAttemptService.scoreCheck(question, request);

        int currentScore = 0;
        if (pvpMatch.getPlayer1().getId().equals(user.getId())) {
            if (result.isCorrect()) {
                pvpMatch.setPlayer1Score((pvpMatch.getPlayer1Score() == null ? 0 : pvpMatch.getPlayer1Score()) + 10);
            }
            currentScore = pvpMatch.getPlayer1Score();
        } else if (pvpMatch.getPlayer2().getId().equals(user.getId())) {
            if (result.isCorrect()) {
                pvpMatch.setPlayer2Score((pvpMatch.getPlayer2Score() == null ? 0 : pvpMatch.getPlayer2Score()) + 10);
            }
            currentScore = pvpMatch.getPlayer2Score();
        }

        pvpMatchRepository.save(pvpMatch);

        return QuizProgressResponse.builder()
                .userId(user.getId())
                .currentScore(currentScore)
                .isCorrect(result.isCorrect())
                .questionId(request.getQuestionId())
                .build();
    }
    @Transactional
    // ?? H�m API d�nh ri�ng cho m�n h�nh PvP QuizActivity l?y d?
    public ExamPvpDisplayResponse startPvpExam (Integer matchId, User playerId) {
        PvpMatch pvpMatch = pvpMatchRepository.findById(matchId).orElseThrow(() ->new RuntimeException("L?I: Kh�ng t�m th?y PVPMatch : "));
        User player2 = pvpMatch.getPlayer2();
        User player1 = pvpMatch.getPlayer1();
        var exam = examRepository.findById(pvpMatch.getExam().getId()).orElseThrow(() -> new RuntimeException("L?I: Kh�ng t�m th?y b�i thi (Exam) v?i ID: " + pvpMatch.getId()));

        if (!exam.getActive()) {
            throw new IllegalStateException("Exam has been stopped");
        }

        boolean hasOngoing = examAttemptRepository.existsByUserIdAndCompleteFalse(player2.getId());
        boolean hasOngoing2 = examAttemptRepository.existsByUserIdAndCompleteFalse(player1.getId());


        if (hasOngoing && hasOngoing2) {
            throw new IllegalStateException("You have an ongoing attempt");
        }
        ExamAttempt attempt = ExamAttempt.builder()
                .user(player1)
                .exam(exam)
                .complete(false)
                .startedAt(LocalDateTime.now())
                .build();
        examAttemptRepository.save(attempt);

        ExamAttempt attempt2 = ExamAttempt.builder()
                .user(player2)
                .exam(exam)
                .complete(false)
                .startedAt(LocalDateTime.now())
                .build();
        examAttemptRepository.save(attempt2);

        // Link attempts to the match
        pvpMatch.setPlayer1Attempt(attempt);
        pvpMatch.setPlayer2Attempt(attempt2);
        pvpMatchRepository.save(pvpMatch);

        return examMapper.toExamPvpDisplayResponse(exam,attempt,attempt2);
    }

    public Object submitPvP(ExamSubmitRequest request, Integer matchId){

    }
}
