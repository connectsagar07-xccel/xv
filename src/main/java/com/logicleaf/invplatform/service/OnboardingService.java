package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.model.FounderProfileRequest;
import com.logicleaf.invplatform.model.InvestorProfileRequest;

public interface OnboardingService {
    void createFounderProfile(String userId, FounderProfileRequest request);
    void createInvestorProfile(String userId, InvestorProfileRequest request);
}