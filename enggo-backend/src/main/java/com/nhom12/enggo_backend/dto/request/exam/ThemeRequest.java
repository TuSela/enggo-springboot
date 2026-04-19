package com.nhom12.enggo_backend.dto.request.exam;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ThemeRequest {
    @NotNull
    @NotEmpty
    String themeName;
    @NotNull
    @NotEmpty
    String category;
    String themeDescription;
}
