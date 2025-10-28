package com.logicleaf.invplatform.dto;

import com.logicleaf.invplatform.model.Role;
import com.logicleaf.invplatform.model.User;
import lombok.Data;

@Data
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String id;
    private String name;
    private String email;
    private Role role;
    private boolean isVerified;

    public JwtAuthenticationResponse(String accessToken, User user) {
        this.accessToken = accessToken;
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.isVerified = user.isVerified();
    }
}