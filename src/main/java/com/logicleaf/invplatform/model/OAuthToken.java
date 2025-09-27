package com.logicleaf.invplatform.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "oauth_tokens")
public class OAuthToken {

    @Id
    private String id;

    private String accessToken;
    private String refreshToken;
    private Instant expiresAt; // when access token expires
    private String apiDomain;  // e.g. https://www.zohoapis.in OR https://www.zohoapis.com
}
