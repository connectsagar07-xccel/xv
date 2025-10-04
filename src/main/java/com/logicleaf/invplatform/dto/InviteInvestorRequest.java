package com.logicleaf.invplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteInvestorRequest {
    @NotBlank
    @Email
    private String investorEmail;
}