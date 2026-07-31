package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.exception.ForbiddenOperationException;
import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.repository.AdministratorFinancialRepository;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FindAdministratorFinancialSummaryUseCase {

    private final AdministratorRepository administratorRepository;
    private final AdministratorFinancialRepository financialRepository;

    public FindAdministratorFinancialSummaryUseCase(
            AdministratorRepository administratorRepository,
            AdministratorFinancialRepository financialRepository) {
        this.administratorRepository = administratorRepository;
        this.financialRepository = financialRepository;
    }

    @Transactional(readOnly = true)
    public AdministratorFinancialRepository.Summary execute(
            Long administratorId, String authenticatedEmail) {
        Administrator administrator = administratorRepository.findById(administratorId)
                .orElseThrow(() -> new ResourceNotFoundException("Administrator not found"));
        if (!Boolean.TRUE.equals(administrator.getActive())) {
            throw new BusinessRuleException("Inactive administrator cannot view financial data");
        }
        if (!administrator.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new ForbiddenOperationException(
                    "Authenticated administrator cannot view another administrator's financial data");
        }
        return financialRepository.summarize(administratorId);
    }
}
