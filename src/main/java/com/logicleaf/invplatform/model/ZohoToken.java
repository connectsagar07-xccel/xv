package com.logicleaf.invplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "zoho_tokens")
public class ZohoToken {

    @Id
    private String id = "zoho"; // always single record

    private String accessToken;
    private String refreshToken;
    private String apiDomain;
    private Instant expiryTime; // when access token expires

}
