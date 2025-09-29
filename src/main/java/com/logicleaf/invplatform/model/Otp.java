package com.logicleaf.invplatform.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Document(collection = "otps")
public class Otp {
    @Id
    private String id;
    private String email;
    private String code;
    private Instant expiryDate;
}