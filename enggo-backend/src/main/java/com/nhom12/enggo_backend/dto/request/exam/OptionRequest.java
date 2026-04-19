package com.nhom12.enggo_backend.dto.request.exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OptionRequest {
    String optionText;
    @JsonProperty("isCorrect")
    boolean correct;
}
