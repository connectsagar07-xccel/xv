package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.model.*;
import com.logicleaf.invplatform.repository.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConnectionService {

        private final StartupInvestorMappingRepository mappingRepository;
        private final UserRepository userRepository;
        private final StartupRepository startupRepository;
        private final InvestorRepository investorRepository;
        private final MailService mailService;

        // Founder invites an investor
        public StartupInvestorMapping inviteInvestor(String founderEmail, String investorEmail,
                        InvestorRole investorRole) {
                User founderUser = userRepository.findByEmail(founderEmail)
                                .orElseThrow(() -> new RuntimeException("Founder not found."));
                Startup startup = startupRepository.findByFounderUserId(founderUser.getId())
                                .orElseThrow(() -> new RuntimeException("Startup not found."));

                User investorUser = userRepository.findByEmail(investorEmail)
                                .orElseThrow(() -> new RuntimeException("Investor not found."));
                Investor investor = investorRepository.findByUserId(investorUser.getId())
                                .orElseThrow(() -> new RuntimeException("Investor profile not found."));

                StartupInvestorMapping mapping = StartupInvestorMapping.builder()
                                .startupId(startup.getId())
                                .investorId(investor.getId())
                                .investorRole(investorRole) // ✅ added
                                .status(MappingStatus.INVITED)
                                .build();

                mapping = mappingRepository.save(mapping);

                try {
                        mailService.sendConnectionEmail(investorUser.getEmail(), startup.getStartupName(),
                                        mapping.getId(), true);
                } catch (Exception e) {
                        System.err.println("Failed to send invite email: " + e.getMessage());
                }

                return mapping;
        }

        // Investor requests connection with a startup
        public StartupInvestorMapping requestConnection(String investorEmail, String startupId,
                        InvestorRole investorRole) {
                User investorUser = userRepository.findByEmail(investorEmail)
                                .orElseThrow(() -> new RuntimeException("Investor not found."));
                Investor investor = investorRepository.findByUserId(investorUser.getId())
                                .orElseThrow(() -> new RuntimeException("Investor profile not found."));
                Startup startup = startupRepository.findById(startupId)
                                .orElseThrow(() -> new RuntimeException("Startup not found."));

                StartupInvestorMapping mapping = StartupInvestorMapping.builder()
                                .startupId(startup.getId())
                                .investorId(investor.getId())
                                .investorRole(investorRole) // ✅ added
                                .status(MappingStatus.PENDING)
                                .build();

                mapping = mappingRepository.save(mapping);

                User founderUser = userRepository.findById(startup.getFounderUserId())
                                .orElseThrow(() -> new RuntimeException("Founder not found."));

                try {
                        mailService.sendConnectionEmail(founderUser.getEmail(), investor.getFirmName(), mapping.getId(),
                                        false);
                } catch (Exception e) {
                        System.err.println("Failed to send request email: " + e.getMessage());
                }

                return mapping;
        }

        // Founder approves a PENDING request
        public StartupInvestorMapping approveConnectionPublic(String mappingId) {
                StartupInvestorMapping m = mappingRepository.findById(mappingId)
                                .orElseThrow(() -> new RuntimeException("Connection not found."));

                if (m.getStatus() != MappingStatus.PENDING) {
                        throw new RuntimeException("Only pending requests can be approved by founder.");
                }

                // load both sides for notifications
                Startup startup = startupRepository.findById(m.getStartupId())
                                .orElseThrow(() -> new RuntimeException("Startup not found."));
                Investor investor = investorRepository.findById(m.getInvestorId())
                                .orElseThrow(() -> new RuntimeException("Investor not found."));
                User founderUser = userRepository.findById(startup.getFounderUserId())
                                .orElseThrow(() -> new RuntimeException("Founder user not found."));
                User investorUser = userRepository.findById(investor.getUserId())
                                .orElseThrow(() -> new RuntimeException("Investor user not found."));

                m.setStatus(MappingStatus.ACTIVE);
                StartupInvestorMapping saved = mappingRepository.save(m);

                // notify investor that founder approved
                try {
                        mailService.sendConnectionStatusEmail(
                                        founderUser.getEmail(),
                                        founderUser.getName(),
                                        investorUser.getEmail(),
                                        startup.getStartupName(),
                                        "approved",
                                        "founder");
                } catch (Exception ignored) {
                }

                return saved;
        }

        // Investor accepts an INVITED invite
        public StartupInvestorMapping acceptInvitationPublic(String mappingId) {
                StartupInvestorMapping m = mappingRepository.findById(mappingId)
                                .orElseThrow(() -> new RuntimeException("Invitation not found."));

                if (m.getStatus() != MappingStatus.INVITED) {
                        throw new RuntimeException("Only invited connections can be accepted by investor.");
                }

                Startup startup = startupRepository.findById(m.getStartupId())
                                .orElseThrow(() -> new RuntimeException("Startup not found."));
                Investor investor = investorRepository.findById(m.getInvestorId())
                                .orElseThrow(() -> new RuntimeException("Investor not found."));
                User founderUser = userRepository.findById(startup.getFounderUserId())
                                .orElseThrow(() -> new RuntimeException("Founder user not found."));
                User investorUser = userRepository.findById(investor.getUserId())
                                .orElseThrow(() -> new RuntimeException("Investor user not found."));

                m.setStatus(MappingStatus.ACTIVE);
                StartupInvestorMapping saved = mappingRepository.save(m);

                // notify founder that investor accepted
                try {
                        mailService.sendConnectionStatusEmail(
                                        investorUser.getEmail(),
                                        investorUser.getName(),
                                        founderUser.getEmail(),
                                        startup.getStartupName(),
                                        "accepted",
                                        "investor");
                } catch (Exception ignored) {
                }

                return saved;
        }

        // Founder rejects a PENDING request (delete mapping)
        public void rejectByFounder(String mappingId) {
                StartupInvestorMapping m = mappingRepository.findById(mappingId)
                                .orElseThrow(() -> new RuntimeException("Connection not found."));

                Startup startup = startupRepository.findById(m.getStartupId())
                                .orElseThrow(() -> new RuntimeException("Startup not found."));
                Investor investor = investorRepository.findById(m.getInvestorId())
                                .orElseThrow(() -> new RuntimeException("Investor not found."));
                User founderUser = userRepository.findById(startup.getFounderUserId())
                                .orElseThrow(() -> new RuntimeException("Founder user not found."));
                User investorUser = userRepository.findById(investor.getUserId())
                                .orElseThrow(() -> new RuntimeException("Investor user not found."));

                // optional status gate:
                if (m.getStatus() != MappingStatus.PENDING) {
                        throw new RuntimeException("Only pending requests can be rejected by founder.");
                }

                // notify investor, then delete
                try {
                        mailService.sendRejectionEmail(
                                        investorUser.getEmail(), founderUser.getName(), startup.getStartupName(), true);
                } catch (Exception ignored) {
                }

                mappingRepository.deleteById(mappingId);
        }

        // Investor rejects an INVITED invite (delete mapping)
        public void rejectByInvestor(String mappingId) {
                StartupInvestorMapping m = mappingRepository.findById(mappingId)
                                .orElseThrow(() -> new RuntimeException("Invitation not found."));

                Startup startup = startupRepository.findById(m.getStartupId())
                                .orElseThrow(() -> new RuntimeException("Startup not found."));
                Investor investor = investorRepository.findById(m.getInvestorId())
                                .orElseThrow(() -> new RuntimeException("Investor not found."));
                User founderUser = userRepository.findById(startup.getFounderUserId())
                                .orElseThrow(() -> new RuntimeException("Founder user not found."));
                User investorUser = userRepository.findById(investor.getUserId())
                                .orElseThrow(() -> new RuntimeException("Investor user not found."));

                if (m.getStatus() != MappingStatus.INVITED) {
                        throw new RuntimeException("Only invited connections can be rejected by investor.");
                }

                try {
                        mailService.sendRejectionEmail(
                                        founderUser.getEmail(), investorUser.getName(), startup.getStartupName(),
                                        false);
                } catch (Exception ignored) {
                }

                mappingRepository.deleteById(mappingId);
        }
}
