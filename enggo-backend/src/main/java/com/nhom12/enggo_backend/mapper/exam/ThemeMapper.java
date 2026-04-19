package com.nhom12.enggo_backend.mapper.exam;

import com.nhom12.enggo_backend.dto.request.exam.ThemeRequest;
import com.nhom12.enggo_backend.dto.response.exam.ThemeResponse;
import com.nhom12.enggo_backend.entity.exam.Theme;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ThemeMapper {
    Theme toTheme(ThemeRequest theme);
    ThemeResponse toThemeResponse(Theme theme);
    void updateTheme(@MappingTarget Theme theme, ThemeRequest themeRequest);
}
