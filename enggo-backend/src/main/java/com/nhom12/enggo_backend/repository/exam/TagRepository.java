package com.nhom12.enggo_backend.repository.exam;

import com.nhom12.enggo_backend.entity.exam.QuestionTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<QuestionTag,Long> {
    void deleteByQuestionId(Integer questionId);
}
