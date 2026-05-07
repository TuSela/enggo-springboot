package com.nhom12.enggo_backend.controller.exam;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.response.PageResponse;
import com.nhom12.enggo_backend.dto.response.exam.ExamDetailResponse;
import com.nhom12.enggo_backend.dto.response.exam.ExamResponse;
import com.nhom12.enggo_backend.repository.exam.ExamRepository;
import com.nhom12.enggo_backend.service.exam.ExamService;
import com.nhom12.enggo_backend.service.exam.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExcelImportService  excelImportService;
    private final ExamService examService;

    @PostMapping("/import")
    ApiResponse<ExamDetailResponse> importExamExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.<ExamDetailResponse>builder()
                .result(examService.createExam(file))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteExam(@PathVariable("id") Integer id){
        examService.deleteExam(id);
        return ApiResponse.<String>builder()
                .result("success")
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ExamDetailResponse> getExamDetail(@PathVariable("id")  Integer id){
        return ApiResponse.<ExamDetailResponse>builder()
                .result(examService.getExamDetail(id))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<PageResponse<ExamResponse>> getAllExams(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<Integer> themeIds,
            @RequestParam(required = false) List<Integer> skillIds,
            @RequestParam(required = false) List<Byte> diffs
    ) {
        return ApiResponse.<PageResponse<ExamResponse>>builder()
                .result(examService.getAllExams(page,size,sortBy,direction,themeIds,skillIds,diffs))
                .build();
    }
}
