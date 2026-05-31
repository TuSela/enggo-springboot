package com.nhom12.enggo_backend.service.exam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom12.enggo_backend.dto.request.exam.ExamAnswerRequest;
import com.nhom12.enggo_backend.dto.request.exam.ExamSubmitRequest;
import com.nhom12.enggo_backend.dto.request.exam.FillBlankSubmitRequest;
import com.nhom12.enggo_backend.dto.request.exam.MatchingSubmitRequest;
import com.nhom12.enggo_backend.dto.response.PageResponse;
import com.nhom12.enggo_backend.dto.response.exam.*;
import com.nhom12.enggo_backend.entity.exam.*;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.mapper.exam.ExamAttemptMapper;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.exam.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
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

            ScoreCheck result = scoreCheck(question, answer, exam);
            if (result.isCorrect()) {
                correctCount++;
            }

            details.add(saveDetail(attempt, question, answer, result));
        }

        BigDecimal totalScore = BigDecimal.ZERO;
        if (exam.getTotalQuestions() > 0) {
            totalScore = BigDecimal.valueOf(10)
                    .multiply(BigDecimal.valueOf(correctCount))
                    .divide(BigDecimal.valueOf(exam.getTotalQuestions()), RoundingMode.HALF_UP);
        }

        attempt.setCorrectAnswersCount(correctCount);
        attempt.setCompletedAt(isTimeOut ? LocalDateTime.now() : LocalDateTime.now().plusMinutes(duration));
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

    private ScoreCheck scoreMultipleChoice (Question question, ExamAnswerRequest answer, Exam exam) {
        BigDecimal pointsPerQuestion = BigDecimal.TEN
                .divide(BigDecimal.valueOf(exam.getTotalQuestions()), 2, BigDecimal.ROUND_HALF_UP);
        if (answer.getSelectedOptionId() == null) return ScoreCheck.isCorrect(BigDecimal.ZERO, false);

        //kiem tra co tim dc id nam trg question ko va tim kiem theo id day cot isCorrect
        boolean isCorrect = question.getOptions().stream()
                .filter(option -> option.getId().equals(answer.getSelectedOptionId()))
                .findFirst()
                .map(QuestionOption::isCorrect)
                .orElse(false);

        return ScoreCheck.isCorrect(isCorrect ? pointsPerQuestion : BigDecimal.ZERO, isCorrect);
    }

    private ScoreCheck scoreCheck(Question question, ExamAnswerRequest answer, Exam exam) {
        return switch (question.getQuestionType()) {
            case "FILL_BLANK" ->   scoreFillBlank(question, answer, exam);
            case "MATCHING" ->  scoreMatching(question, answer, exam);
            default ->  scoreMultipleChoice(question, answer,  exam);
        };
    }

    private ScoreCheck scoreFillBlank (Question question, ExamAnswerRequest answer, Exam exam) {
        BigDecimal pointsPerQuestion = BigDecimal.TEN
                .divide(BigDecimal.valueOf(exam.getTotalQuestions()), 2, BigDecimal.ROUND_HALF_UP);
        // map du lieu da nhap vao
        Map<Integer, FillBlankSubmitRequest> userAnswers = Optional.ofNullable(answer.getFillBlanks())
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(FillBlankSubmitRequest::getBlankId, b -> b, (b1, b2) -> b1));

        //tu question loc ra nhung option thoa man dieu kien va so sanh voi map o tren de ket luan
        List<FillBlankResultResponse> blankResults = question.getOptions().stream()
                .filter(o -> o.getOption_group() != null && o.getOption_group().startsWith("BLANK_"))
                .map(correctOption -> {
                    FillBlankResultResponse blankResult = new FillBlankResultResponse();

                    Integer correctPosition = Integer.parseInt(correctOption.getOption_group().replace("BLANK_", ""));
                    String correctAnswer = correctOption.getOptionText();

                    FillBlankSubmitRequest userAnswer = userAnswers.get(correctOption.getId());

                    blankResult.setBlankId(correctOption.getId());
                    blankResult.setPosition(correctPosition);
                    blankResult.setCorrectValue(correctAnswer);

                    if (userAnswer != null) {
                        blankResult.setUserInput(userAnswer.getUserInput());
                        boolean isCorrect = correctPosition.equals(userAnswer.getPosition())
                                && userAnswer.getUserInput() != null
                                && correctAnswer.trim().equalsIgnoreCase(userAnswer.getUserInput().trim());
                        blankResult.setIsCorrect(isCorrect);
                    } else {
                        blankResult.setIsCorrect(false);
                        blankResult.setUserInput("");
                    }

                    return blankResult;
                }).toList();

        if (answer.getFillBlanks() == null || answer.getFillBlanks().isEmpty())
            return ScoreCheck.ofFillBlank(BigDecimal.ZERO, false, null, blankResults);

        boolean allCorrect = blankResults.stream().allMatch(FillBlankResultResponse::getIsCorrect);
        return ScoreCheck.ofFillBlank(allCorrect ? pointsPerQuestion : BigDecimal.ZERO, allCorrect, null, blankResults);
    }

    private ScoreCheck scoreMatching (Question question, ExamAnswerRequest answer, Exam exam) {
        BigDecimal pointsPerQuestion = BigDecimal.TEN
                .divide(BigDecimal.valueOf(exam.getTotalQuestions()), 2, BigDecimal.ROUND_HALF_UP);
        //tao map option
        Map<Integer, QuestionOption> allOptionsMap = question.getOptions().stream()
                .collect(Collectors.toMap(QuestionOption::getId, o -> o));

        //map ket qua bai lam
        Map<Integer, MatchingSubmitRequest> userMatchings = Optional.ofNullable(answer.getMatchings())
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(MatchingSubmitRequest::getLeftId, m -> m, (m1, m2) -> m1));

        Map<String, QuestionOption> rightOptionsByMatchKey = question.getOptions().stream()
                .filter(o -> o.getOption_group() != null
                                && o.getOption_group().equalsIgnoreCase("RIGHT")
                                && o.getMatch_key() != null)
                .collect(Collectors.toMap(QuestionOption::getMatch_key, o -> o, (o1, o2) -> o1));

        List<MatchingResultResponse> matchingResults = question.getOptions().stream()
                .filter(o -> o.getOption_group() != null
                        && o.getOption_group().equalsIgnoreCase("LEFT")
                        && o.getMatch_key() != null)
                .map(leftOption -> {
                    MatchingResultResponse matchingResult = new MatchingResultResponse();
                    matchingResult.setLeftId(leftOption.getId());
                    matchingResult.setLeftText(leftOption.getOptionText());

                    String leftKey =  leftOption.getMatch_key();

                    QuestionOption correctRight = rightOptionsByMatchKey.get(leftKey);

                    matchingResult.setCorrectRightId(correctRight != null ? correctRight.getId() : null);
                    matchingResult.setCorrectRightText(correctRight != null ? correctRight.getOptionText() : null);

                    MatchingSubmitRequest userAnswer = userMatchings.get(leftOption.getId());

                    if (userAnswer == null) {
                        matchingResult.setUserRightId(null);
                        matchingResult.setUserRightText(null);
                        matchingResult.setIsCorrect(false);
                    } else {
                        matchingResult.setUserRightId(userAnswer.getRightId());
                        QuestionOption userRightOption = allOptionsMap.get(userAnswer.getRightId());
                        matchingResult.setUserRightText(userRightOption != null ? userRightOption.getOptionText() : null);

                        String rightKey = userRightOption != null ? userRightOption.getMatch_key() : null;
                        boolean isCorrect = leftKey != null && leftKey.equalsIgnoreCase(rightKey);
                        matchingResult.setIsCorrect(isCorrect);
                    }

                    return matchingResult;
                }).toList();

        if (answer.getMatchings() == null || answer.getMatchings().isEmpty()) {
            return ScoreCheck.ofMatching(BigDecimal.ZERO, false, null, matchingResults);
        }

        boolean allCorrect = matchingResults.stream().allMatch(MatchingResultResponse::getIsCorrect);
        return ScoreCheck.ofMatching(
                allCorrect ? pointsPerQuestion : BigDecimal.ZERO,
                allCorrect,
                null,
                matchingResults
        );
    }

    public ExamAttemptDetail saveDetail(ExamAttempt attempt, Question question, ExamAnswerRequest answer, ScoreCheck result) {
        String userInput = null;

        if ("MATCHING".equals(question.getQuestionType())) {
            userInput = serializeToJson(answer.getMatchings());
        } else if ("FILL_BLANK".equals(question.getQuestionType())) {
            userInput = serializeToJson(answer.getFillBlanks());
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
        if (!attempt.getComplete()) {
            throw new RuntimeException("Attempt with id " + attemptId + " not found");
        }

        Map<Integer,Integer> questionOrderMap = attempt.getExam().getExamQuestions().stream()
                .collect(Collectors.toMap(
                        eq -> eq.getQuestion().getId(),
                        ExamQuestion::getOrderPriority
                ));

        List<ExamAttemptDetail> details = examAttemptDetailRepository.findByAttemptId(attemptId);

        List<ExamAttemptDetailResponse> detailsResponses = details.stream()
                .map(detail -> {
                    ExamAttemptDetailResponse detailResponse = new ExamAttemptDetailResponse();
                    detailResponse.setScore(detail.getScore());

                    Integer orderPriority = questionOrderMap.get(detail.getQuestion().getId());
                    detailResponse.setOrderPriority(orderPriority);

                    QuestionResultResponse questionResult = buildQuestionResultFromDetail(detail);
                    detailResponse.setQuestion(questionResult);

                    return detailResponse;
                })
                .sorted(Comparator.comparing(ExamAttemptDetailResponse::getOrderPriority))
                .toList();

        ExamSubmitResponse response = new ExamSubmitResponse();
        response.setAttemptId(attemptId);
        response.setExamId(attempt.getExam().getId());
        response.setTotalScore(attempt.getTotalScore());
        response.setCorrectAnswersCount(attempt.getCorrectAnswersCount());
        response.setStartedAt(attempt.getStartedAt());
        response.setCompletedAt(attempt.getCompletedAt());
        response.setDetail(detailsResponses);

        return response;
    }

    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<FillBlankSubmitRequest> parseBlanks(String json) {
        try {
            return objectMapper.readValue(json,
                    new TypeReference<List<FillBlankSubmitRequest>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<MatchingSubmitRequest> parseMatchings(String json) {
        try {
            return objectMapper.readValue(json,
                    new TypeReference<List<MatchingSubmitRequest>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private QuestionResultResponse buildQuestionResultFromDetail(ExamAttemptDetail detail) {
        var question = detail.getQuestion();
        var response = new QuestionResultResponse();
        response.setId(question.getId());
        response.setQuestionType(question.getQuestionType());
        response.setContent(question.getContent());
        response.setExplanation(question.getExplanation());

        switch (question.getQuestionType()) {

            case "MULTIPLE_CHOICE" -> {
                response.setMultipleOptions(
                        question.getOptions().stream()
                                .map(o -> examAttemptMapper.toMultipleOptionResultResponse(detail, o))
                                .toList()
                );
            }

            case "FILL_BLANK" -> {
                // Parse userInput từ detail → reconstruct answer
                List<FillBlankSubmitRequest> blanks = parseBlanks(detail.getUserInput());

                // Tái sử dụng scoreFillBlank để check từng blank
                ExamAnswerRequest answer = new ExamAnswerRequest();
                answer.setFillBlanks(blanks);

                ScoreCheck result = scoreFillBlank(question, answer, detail.getAttempt().getExam());

                response.setFillBlankOptions(result.getBlankResults());
            }

            case "MATCHING" -> {
                // Parse userInput từ detail → reconstruct answer
                List<MatchingSubmitRequest> matchings = parseMatchings(detail.getUserInput());

                // Tái sử dụng scoreMatching để check từng cặp
                ExamAnswerRequest answer = new ExamAnswerRequest();
                answer.setMatchings(matchings);

                ScoreCheck result = scoreMatching(question, answer, detail.getAttempt().getExam());

                response.setMatchingResults(result.getMatchingResults());
            }
        }
        return response;
    }

    public PageResponse<ExamSubmitResponse> getAllAttempts(int page, int size, String field, String direction) {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        var user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Username not found"));

        List<String> allowFields = List.of("totalScore", "completedAt");
        String sortField = allowFields.contains(field) ? field : "completedAt";

        Sort sort = direction.equalsIgnoreCase("ASC")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Integer> ids = examAttemptRepository.findAllIds(pageable, user.getId());
        if (ids.isEmpty()) return PageResponse.of(new PageImpl<>(Collections.emptyList(), pageable, 0));

        List<Integer> sortedIds = ids.getContent();
        List<ExamAttempt>  content = examAttemptRepository.findByIds(sortedIds);

        List<ExamAttempt> strictSortedContent = content.stream()
                .sorted(Comparator.comparingInt(attempt -> sortedIds.indexOf(attempt.getId())))
                .toList();

        List<ExamSubmitResponse> responses = examAttemptMapper.toExamSubmitResponses(strictSortedContent);

        Page<ExamSubmitResponse> responsePage = new PageImpl<>(responses, pageable, ids.getTotalElements());

        return PageResponse.of(responsePage);
    }
}
