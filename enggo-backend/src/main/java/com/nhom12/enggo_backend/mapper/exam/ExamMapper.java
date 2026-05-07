package com.nhom12.enggo_backend.mapper.exam;

import com.nhom12.enggo_backend.dto.response.UserMinimalResponse;
import com.nhom12.enggo_backend.dto.response.exam.*;
import com.nhom12.enggo_backend.entity.exam.Exam;
import com.nhom12.enggo_backend.entity.exam.ExamQuestion;
import com.nhom12.enggo_backend.entity.exam.ExamTag;
import com.nhom12.enggo_backend.entity.exam.Theme;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class})
public interface ExamMapper {
    default List<ThemeResponse> mapThemes(List<ExamTag> examTags) {
        if (examTags == null || examTags.isEmpty()) {
            return Collections.emptyList();
        }

        return examTags.stream()
                .map(t-> new ThemeResponse(
                        t.getTheme().getId(),
                        t.getTheme().getThemeName()))
                .distinct().toList();
    }

    default List<SkillResponse> mapSkills(List<ExamTag> examTags) {
        if (examTags == null || examTags.isEmpty()) {
            return Collections.emptyList();
        }

        return examTags.stream()
                .map(s -> new SkillResponse(
                        s.getSkill().getId(),
                        s.getSkill().getSkillName()))
                .distinct().toList();
    }

    @Mapping(target="createdBy.username", source = "createdBy.username")
    @Mapping(target = "createdBy.avatarUrl", source = "createdBy.avatarUrl")
    @Mapping(target = "themes", expression = "java(mapThemes(exam.getExamTags()))")
    @Mapping(target = "skills", expression = "java(mapSkills(exam.getExamTags()))")
    @Mapping(target = "questions", source = "examQuestions")
    ExamDetailResponse toExamDetailResponse(Exam exam);

    @Mapping(target = "questionDetails", source = "question")
    @Mapping(target = "orderPriority", source = "orderPriority")
    ExamQuestionResponse mapExamQuestion(ExamQuestion eq);

    @Mapping(target = "themes", expression = "java(mapThemes(exam.getExamTags()))")
    @Mapping(target = "skills", expression = "java(mapSkills(exam.getExamTags()))")
    ExamResponse toExamResponse(Exam exam);

    List<ExamResponse> toExamResponses(List<Exam> exams);
}
