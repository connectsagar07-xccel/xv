package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.model.*;
import java.util.Optional;

public interface AuthService {
    User signup(SignUpRequest signUpRequest, Role role);
    JwtAuthenticationResponse login(LoginRequest loginRequest);
    Optional<JwtAuthenticationResponse> refreshToken(TokenRefreshRequest tokenRefreshRequest);
    boolean verifyOtp(VerifyOtpRequest verifyOtpRequest);
}