package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.model.*;
import com.logicleaf.invplatform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConnectionService {

    @Autowired
    private StartupInvestorMappingRepository mappingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StartupRepository startupRepository;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private NotificationService notificationService; // For sending invites

    // Founder invites an investor
    public StartupInvestorMapping inviteInvestor(String founderEmail, String investorEmail) {
        User founderUser = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new RuntimeException("Founder not found."));
        Startup startup = startupRepository.findByFounderUserId(founderUser.getId())
                .orElseThrow(() -> new RuntimeException("Founder profile not found."));

        Optional<User> investorUserOpt = userRepository.findByEmail(investorEmail);

        // If investor doesn't exist, we can create a placeholder or send an invite to join the platform.
        // For now, we'll assume the investor must have an account.
        User investorUser = investorUserOpt.orElseThrow(() -> new RuntimeException("Investor not found. Please ask them to sign up first."));
        Investor investor = investorRepository.findByUserId(investorUser.getId())
                .orElseThrow(() -> new RuntimeException("Investor profile not found."));

        StartupInvestorMapping mapping = StartupInvestorMapping.builder()
                .startupId(startup.getId())
                .investorId(investor.getId())
                .status(MappingStatus.INVITED)
                .build();

        // In a real app, send an email notification to the investor
        // notificationService.sendInvitation(investorEmail, startup.getStartupName());

        return mappingRepository.save(mapping);
    }

    // Founder approves a pending request from an investor
    public StartupInvestorMapping approveConnection(String founderEmail, String mappingId) {
        User founderUser = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new RuntimeException("Founder not found."));
        Startup startup = startupRepository.findByFounderUserId(founderUser.getId())
                .orElseThrow(() -> new RuntimeException("Founder profile not found."));

        StartupInvestorMapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new RuntimeException("Connection request not found."));

        // Ensure the mapping belongs to the founder's startup and is in PENDING state
        if (!mapping.getStartupId().equals(startup.getId()) || mapping.getStatus() != MappingStatus.PENDING) {
            throw new RuntimeException("Invalid request. Not a pending connection for this startup.");
        }

        mapping.setStatus(MappingStatus.ACTIVE);
        return mappingRepository.save(mapping);
    }

    // Investor requests to connect with a startup
    public StartupInvestorMapping requestConnection(String investorEmail, String startupId) {
        User investorUser = userRepository.findByEmail(investorEmail)
                .orElseThrow(() -> new RuntimeException("Investor not found."));
        Investor investor = investorRepository.findByUserId(investorUser.getId())
                .orElseThrow(() -> new RuntimeException("Investor profile not found."));

        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new RuntimeException("Startup not found."));

        StartupInvestorMapping mapping = StartupInvestorMapping.builder()
                .startupId(startup.getId())
                .investorId(investor.getId())
                .status(MappingStatus.PENDING) // Founder needs to approve this
                .build();

        // In a real app, send a notification to the founder
        // notificationService.sendConnectionRequest(startup.getFounderUserId(), investor.getFirmName());

        return mappingRepository.save(mapping);
    }

    // Investor accepts an invitation from a startup
    public StartupInvestorMapping acceptInvitation(String investorEmail, String mappingId) {
        User investorUser = userRepository.findByEmail(investorEmail)
                .orElseThrow(() -> new RuntimeException("Investor not found."));
        Investor investor = investorRepository.findByUserId(investorUser.getId())
                .orElseThrow(() -> new RuntimeException("Investor profile not found."));

        StartupInvestorMapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new RuntimeException("Invitation not found."));

        // Ensure the mapping is for this investor and is in INVITED state
        if (!mapping.getInvestorId().equals(investor.getId()) || mapping.getStatus() != MappingStatus.INVITED) {
            throw new RuntimeException("Invalid request. Not an invited connection for this investor.");
        }

        mapping.setStatus(MappingStatus.ACTIVE);
        return mappingRepository.save(mapping);
    }
}