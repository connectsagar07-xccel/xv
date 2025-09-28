package com.logicleaf.invplatform.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username; // can be derived or same as email local-part
    private String name;
    private String email;
    private String phone;
    private String password;
    private Set<String> roles;
    private String otp;
    private Instant otpExpiry;
    private boolean otpVerified;
}
