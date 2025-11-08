package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.dto.InvestorFullResponse;
import com.logicleaf.invplatform.exception.ResourceNotFoundException;
import com.logicleaf.invplatform.model.*;
import com.logicleaf.invplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StartupService {

    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final InvestorRepository investorRepository;
    private final StartupInvestorMappingRepository mappingRepository;

    /**
     * Get all ACTIVE investors of a founder's startup with full joined data (user + investor + mapping).
     */
    public List<InvestorFullResponse> getFullInvestorDataForStartup(String founderEmail) {

        // 1️⃣ Fetch founder
        User founder = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Founder not found."));

        // 2️⃣ Fetch startup
        Startup startup = startupRepository.findByFounderUserId(founder.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Startup not found for this founder."));

        // 3️⃣ Fetch active mappings
        List<StartupInvestorMapping> activeMappings =
                mappingRepository.findByStartupIdAndStatus(startup.getId(), MappingStatus.ACTIVE);

        if (activeMappings.isEmpty()) {
            throw new ResourceNotFoundException("No active investors found for this startup.");
        }

        // 4️⃣ Extract investor IDs
        List<String> investorIds = activeMappings.stream()
                .map(StartupInvestorMapping::getInvestorId)
                .toList();

        // 5️⃣ Fetch investor profiles
        List<Investor> investors = investorRepository.findAllById(investorIds);
        Map<String, Investor> investorMap = investors.stream()
                .collect(Collectors.toMap(Investor::getId, i -> i));

        // 6️⃣ Fetch user details linked to investors
        List<String> userIds = investors.stream().map(Investor::getUserId).toList();
        List<User> users = userRepository.findAllById(userIds);
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 7️⃣ Build DTO list (merge User + Investor + Mapping)
        return activeMappings.stream()
                .map(mapping -> {
                    Investor investor = investorMap.get(mapping.getInvestorId());
                    User user = userMap.get(investor.getUserId());

                    return InvestorFullResponse.builder()
                            // user info
                            .userId(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            // investor info
                            .investorId(investor.getId())
                            .firmName(investor.getFirmName())
                            .investorType(investor.getInvestorType())
                            .ticketSize(investor.getTicketSize())
                            .sectorFocus(investor.getSectorFocus())
                            .aum(investor.getAum())
                            // mapping info
                            .mappingId(mapping.getId())
                            .investorRole(mapping.getInvestorRole())
                            .status(mapping.getStatus())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
