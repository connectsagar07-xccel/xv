package com.logicleaf.invplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String status;
    private String message;
    private TokenResponse token;
    private UserResponse user;
}