package com.nhom12.enggo_backend.mapper.exam;

import com.nhom12.enggo_backend.dto.request.exam.ExamAnswerRequest;
import com.nhom12.enggo_backend.dto.request.exam.ExamSubmitRequest;
import com.nhom12.enggo_backend.dto.response.exam.ExamDetailResponse;
import com.nhom12.enggo_backend.dto.response.exam.ExamSubmitResponse;
import com.nhom12.enggo_backend.entity.exam.ExamAttempt;
import com.nhom12.enggo_backend.entity.exam.ExamAttemptDetail;
import com.nhom12.enggo_backend.entity.exam.Question;
import com.nhom12.enggo_backend.entity.exam.QuestionOption;
import com.nhom12.enggo_backend.service.exam.ScoreCheck;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExamAttemptMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attempt", source = "examAttempt")
    @Mapping(target = "question", source = "question")
    @Mapping(target = "selectedOption", source = "option")
    @Mapping(target = "userInput", source = "userInput")
    @Mapping(target = "correct", source = "result.correct")
    @Mapping(target = "score", source = "result.score")
    ExamAttemptDetail toExamAttemptDetail (
            ExamAttempt examAttempt,
            Question question,
            QuestionOption option,
            String userInput,
            ScoreCheck result);

    @Mapping(target = "attemptId", source = "id")
    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "totalQuestions", source = "exam.totalQuestions")
    ExamSubmitResponse toExamSubmitResponse(ExamAttempt examAttempt);
}
