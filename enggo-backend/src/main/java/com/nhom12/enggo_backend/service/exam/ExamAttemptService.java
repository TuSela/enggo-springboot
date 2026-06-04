package com.nhom12.enggo_backend.service.exam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom12.enggo_backend.dto.request.exam.ExamAnswerRequest;
import com.nhom12.enggo_backend.dto.request.exam.ExamSubmitRequest;
import com.nhom12.enggo_backend.dto.response.exam.ExamSubmitResponse;
import com.nhom12.enggo_backend.entity.exam.*;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.mapper.exam.ExamAttemptMapper;
import com.nhom12.enggo_backend.mapper.exam.ExamMapper;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.exam.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamAttemptService {
    private final ExamAttemptRepository examAttemptRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final ExamAttemptMapper examAttemptMapper;
    private final ObjectMapper objectMapper;
    private final ExamAttemptDetailRepository examAttemptDetailRepository;

    @Transactional
    public ExamSubmitResponse submitExam(ExamSubmitRequest request, Integer examId, Integer attemptId) {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        var user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Username not found"));
        var attempt = examAttemptRepository.findById(attemptId).orElseThrow(() -> new RuntimeException("Exam Attempt Not Found"));
        var exam = examRepository.findById(examId).orElseThrow(() -> new RuntimeException("Exam Not Found"));

        validateExamAttempt(attempt,  user, exam);

        int duration = exam.getDurationMinutes();
        boolean isTimeOut = LocalDateTime.now().isAfter(attempt.getStartedAt().plusMinutes(duration));

        int correctCount = 0;
        BigDecimal pointsPerQuestion = BigDecimal.TEN.divide(BigDecimal.valueOf(exam.getTotalQuestions()), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalScore = BigDecimal.ZERO;

        List<ExamAttemptDetail> details = new ArrayList<>();
        List<ExamSubmitResponse> responses = new ArrayList<>();

        for (ExamAnswerRequest answer : request.getExamAnswers()) {
            var question = questionRepository.findById(answer.getQuestionId()).orElseThrow(() -> new RuntimeException("Question Not Found"));

            //kiem tra cau hoi co trong de thi ko
            boolean isBelong = exam.getExamQuestions().stream()
                    .anyMatch(e -> e.getQuestion().getId().equals(question.getId()));

            if (!isBelong) {
                throw new RuntimeException("Question Not Found in Exam");
            }

            ScoreCheck result = scoreCheck(question, answer);
            if (result.isCorrect()) {
                correctCount++;
                totalScore = totalScore.add(pointsPerQuestion);
            }

            details.add(saveDetail(attempt, question, answer, result));
        }

        totalScore = totalScore.setScale(2, RoundingMode.HALF_UP);
        attempt.setCorrectAnswersCount(correctCount);
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setTotalScore(totalScore);
        attempt.setComplete(true);
        examAttemptRepository.save(attempt);

        examAttemptDetailRepository.saveAll(details);

        return examAttemptMapper.toExamSubmitResponse(attempt);
    }

    private void validateExamAttempt(ExamAttempt attempt, User user, Exam exam) {
        //kiem tra cac dieu kien co ban
        if (attempt.getUser() != null && !attempt.getUser().equals(user)) {
            throw new RuntimeException("Not your attempt");
        }
        if (attempt.getExam() != null && !attempt.getExam().equals(exam)) {
            throw new RuntimeException("Attempt does not belong to this exam");
        }
        if (attempt.getComplete()) {
            throw new RuntimeException("Attempt is already completed");
        }
    }

    private ScoreCheck scoreMultipleChoice (Question question, ExamAnswerRequest answer) {
        if (answer.getSelectedOptionId() == null) return ScoreCheck.notCorrect();

        //kiem tra co tim dc id nam trg question ko va tim kiem theo id day cot isCorrect
        boolean isCorrect = question.getOptions().stream()
                .filter(option -> option.getId().equals(answer.getSelectedOptionId()))
                .findFirst()
                .map(QuestionOption::isCorrect)
                .orElse(false);

        return isCorrect ? ScoreCheck.correct() : ScoreCheck.notCorrect();
    }

    public ScoreCheck scoreCheck(Question question, ExamAnswerRequest answer) {
        return switch (question.getQuestionType()) {
            case "MULTIPLE_CHOICE" ->  scoreMultipleChoice(question, answer);
            case "FILL_BLANK" ->   scoreFillBlank(question, answer);
            case "MATCHING" ->  scoreMatching(question, answer);
            default -> ScoreCheck.notCorrect();
        };
    }
    private ScoreCheck scoreFillBlank (Question question, ExamAnswerRequest answer) {
        if (answer.getFillBlanks() == null || answer.getFillBlanks().isEmpty()) return ScoreCheck.notCorrect();

        //tao 1 map luu id option va chinh ban than option do cho de kiem tra
        Map<Integer, QuestionOption> correctMap = question.getOptions().stream()
                //loc cac option co option_group bat dau bang BLANK_
                .filter(o -> o.getOption_group() != null && o.getOption_group().startsWith("BLANK_"))
                .collect(Collectors.toMap(
                        QuestionOption::getId,
                        o -> o
                ));
        boolean isCorrect = answer.getFillBlanks().stream()
                .allMatch(b -> {
                    QuestionOption correctOption = correctMap.get(b.getBlankId());
                    if (correctOption == null) return false;

                    Integer correctPosition =  Integer.parseInt(correctOption.getOption_group().replace("BLANK_", ""));
                    if (!b.getPosition().equals(correctPosition)) return false;

                    String correctAnswer = correctOption.getOptionText();
                    if (correctAnswer == null) return false;

                    return correctAnswer.trim().equalsIgnoreCase(b.getUserInput().trim());
                });

        return isCorrect ? ScoreCheck.correct() : ScoreCheck.notCorrect();
    }
    private ScoreCheck scoreMatching (Question question, ExamAnswerRequest answer) {
        if (answer.getMatchings() == null || answer.getMatchings().isEmpty()) return ScoreCheck.notCorrect();
        //map id va matchKey
        Map<Integer, String> correctMap = question.getOptions().stream()
                .filter(o -> o.getOption_group() != null && o.getMatch_key() != null)
                .collect(Collectors.toMap(
                        QuestionOption::getId,
                        QuestionOption::getMatch_key
                ));
        //lay leftKey va rightKey theo id va kiem tra co giong nhau ko
        boolean isCorrect = answer.getMatchings().stream()
                .allMatch(m -> {
                    String leftKey = correctMap.get(m.getLeftId());
                    String rightKey = correctMap.get(m.getRightId());
                    if (leftKey == null || rightKey == null) return false;
                    return leftKey.equalsIgnoreCase(rightKey);
                });
        return isCorrect ? ScoreCheck.correct() : ScoreCheck.notCorrect();
    }
    public ExamAttemptDetail saveDetail(ExamAttempt attempt, Question question, ExamAnswerRequest answer, ScoreCheck result) {
        String userInput = null;

        if ("MATCHING".equals(question.getQuestionType())) {
            try {
                userInput = objectMapper.writeValueAsString(answer.getMatchings());
            } catch (Exception e) {
                userInput = answer.getMatchings().toString();
            }
        }

        if ("FILL_BLANK".equals(question.getQuestionType())) {
            try {
                userInput = objectMapper.writeValueAsString(answer.getFillBlanks());
            } catch (Exception e) {
                userInput = answer.getFillBlanks().toString();
            }
        }

        QuestionOption selectedOption = null;
        if (answer.getSelectedOptionId() != null) {
            selectedOption = optionRepository.findById(answer.getSelectedOptionId()).orElse(null);
        }

        return ExamAttemptDetail.builder()
                .attempt(attempt)
                .question(question)
                .userInput(userInput)
                .selectedOption(selectedOption)
                .correct(result.isCorrect())
                .score(result.getScore())
                .build();
    }

    public ExamSubmitResponse getAttemptById(Integer attemptId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId).orElse(null);
        return examAttemptMapper.toExamSubmitResponse(attempt);
    }
}
