package com.nhom12.enggo_backend.controller.exam;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.response.exam.ExamSubmitResponse;
import com.nhom12.enggo_backend.entity.exam.ExamAttempt;
import com.nhom12.enggo_backend.service.exam.ExamAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/attempts")
@RequiredArgsConstructor
public class ExamAttemptController {
    private final ExamAttemptService examAttemptService;

    @GetMapping("/{id}")
    public ApiResponse<ExamSubmitResponse> getAttemptById(@PathVariable("id") Integer id) {
        return ApiResponse.<ExamSubmitResponse>builder()
                .result(examAttemptService.getAttemptById(id))
                .build();
    }
}
