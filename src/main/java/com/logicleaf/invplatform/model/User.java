package com.logicleaf.invplatform.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {

    @Id
    private Long id;

    private String name;

    private String email;

    private String phone;

    private String password;  // store encrypted

    private Role role;

    private String otp;

    private Boolean otpVerified = false; // initially false
}