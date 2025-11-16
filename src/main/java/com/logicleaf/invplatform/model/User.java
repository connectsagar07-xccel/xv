package com.logicleaf.invplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private Role role;
    private boolean isVerified;
    private boolean onboarded; // To track if the user has completed OTP verification
    private String otp;
    private LocalDateTime otpExpiryTime;
    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiryTime;
}