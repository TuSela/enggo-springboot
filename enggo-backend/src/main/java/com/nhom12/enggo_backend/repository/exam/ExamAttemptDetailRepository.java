package com.nhom12.enggo_backend.repository.exam;

import com.nhom12.enggo_backend.entity.exam.ExamAttemptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamAttemptDetailRepository extends JpaRepository<ExamAttemptDetail,Long> {
}
