package com.nhom12.enggo_backend.dto.request.exam;

import lombok.Data;

import java.util.List;

@Data
public class RandomBlueprintRequest {
    Byte difficulty;
    List<String> questionTypes;
    List<Integer> themeIds;
    int totalQuestions;
}
