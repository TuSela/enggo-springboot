package com.nhom12.enggo_backend.dto.response;

import lombok.Data;

@Data
public class UserMinimalResponse {
    private Integer id;
    private String username;
    private String avatarUrl;
}
