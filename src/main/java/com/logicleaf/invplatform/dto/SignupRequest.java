package com.logicleaf.invplatform.dto;

import com.logicleaf.invplatform.model.Role;
import lombok.Data;

@Data
public class SignupRequest {
    private String name;
    private String companyName; // you may store / use later in startup onboarding
    private String email;
    private String phone;
    private String password;
    private Role role; // FOUNDER or INVESTOR; if null default FOUNDER
}
