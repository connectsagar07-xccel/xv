package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.exception.ResourceNotFoundException;
import com.logicleaf.invplatform.model.Investor;
import com.logicleaf.invplatform.repository.InvestorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestorService {

    private final InvestorRepository investorRepository;

    /**
     * ✅ Create or update an investor
     */
    public Investor saveInvestor(Investor investor) {
        return investorRepository.save(investor);
    }

    /**
     * ✅ Fetch investor by ID
     */
    public Investor findById(String investorId) {
        return investorRepository.findById(investorId)
                .orElseThrow(() -> new ResourceNotFoundException("Investor not found with id: " + investorId));
    }

    /**
     * ✅ Fetch investor linked to a specific user (userId)
     */
    public Investor findByUserId(String userId) {
        return investorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Investor not found for userId: " + userId));
    }

    /**
     * ✅ Fetch all investors
     */
    public List<Investor> getAllInvestors() {
        return investorRepository.findAll();
    }

    /**
     * ✅ Delete an investor by ID
     */
    public void deleteInvestor(String investorId) {
        Investor investor = findById(investorId);
        investorRepository.delete(investor);
    }
}
