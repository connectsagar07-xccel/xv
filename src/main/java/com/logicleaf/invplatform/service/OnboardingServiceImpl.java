package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.model.FounderProfileRequest;
import com.logicleaf.invplatform.model.Investor;
import com.logicleaf.invplatform.model.InvestorProfileRequest;
import com.logicleaf.invplatform.model.Startup;
import com.logicleaf.invplatform.repository.InvestorRepository;
import com.logicleaf.invplatform.repository.StartupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {

    private final StartupRepository startupRepository;
    private final InvestorRepository investorRepository;

    @Override
    public void createFounderProfile(String userId, FounderProfileRequest request) {
        if (startupRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("Founder profile already exists for this user.");
        }

        Startup startup = Startup.builder()
                .userId(userId)
                .companyName(request.getCompanyName())
                .startupName(request.getStartupName())
                .sector(request.getSector())
                .stage(request.getStage())
                .fundingRaised(request.getFundingRaised())
                .hqLocation(request.getHqLocation())
                .teamSize(request.getTeamSize())
                .website(request.getWebsite())
                .status("Profile Completed")
                .build();
        startupRepository.save(startup);
    }

    @Override
    public void createInvestorProfile(String userId, InvestorProfileRequest request) {
        if (investorRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("Investor profile already exists for this user.");
        }

        Investor investor = Investor.builder()
                .userId(userId)
                .investorType(request.getInvestorType())
                .firmName(request.getFirmName())
                .ticketSize(request.getTicketSize())
                .sectorFocus(request.getSectorFocus())
                .aum(request.getAum())
                .build();
        investorRepository.save(investor);
    }
}