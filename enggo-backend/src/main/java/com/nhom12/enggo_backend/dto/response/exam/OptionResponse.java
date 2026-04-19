package com.nhom12.enggo_backend.dto.response.exam;

import lombok.Data;

@Data
public class OptionResponse {
    Integer id;
    String optionText;
    Boolean isCorrect;
}
