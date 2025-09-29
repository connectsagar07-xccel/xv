package com.logicleaf.invplatform.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    private String phoneNumber;

    private String password;

    private String role; // e.g., "FOUNDER", "INVESTOR"

    @Builder.Default
    private boolean isVerified = false;

    @Builder.Default
    private boolean profileCompleted = false;
}
