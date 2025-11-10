package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.dto.IntegrationStatusResponse;
import com.logicleaf.invplatform.model.Integration;
import com.logicleaf.invplatform.model.IntegrationStatus;
import com.logicleaf.invplatform.model.IntegrationType;
import com.logicleaf.invplatform.model.Startup;
import com.logicleaf.invplatform.repository.IntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntegrationService {

    private final IntegrationRepository integrationRepository;
    private final StartupService startupService;

    public List<IntegrationStatusResponse> getIntegrationStatuses(String founderEmail) {

        Startup startup = startupService.getStartupByFounderEmail(founderEmail);
            
        List<Integration> integrations = integrationRepository.findByStartupId(startup.getId());

        Map<IntegrationType, Integration> integrationMap = integrations.stream()
                .collect(Collectors.toMap(Integration::getIntegrationType, i -> i));

        List<IntegrationStatusResponse> result = new ArrayList<>();

        for (IntegrationType type : IntegrationType.values()) {
            Integration integration = integrationMap.get(type);

            IntegrationStatusResponse response = IntegrationStatusResponse.builder()
                    .integrationType(type)
                    .status(integration != null ? integration.getStatus() : IntegrationStatus.DISCONNECTED)
                    .lastSyncTime(integration != null ? integration.getLastSyncTime() : null)
                    .build();

            result.add(response);
        }

        return result;
    }
}
