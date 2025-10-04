package com.logicleaf.invplatform.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class OAuthToken {
    private String accessToken;
    private String refreshToken;
    private String apiDomain;
    private Instant expiresAt;
}
