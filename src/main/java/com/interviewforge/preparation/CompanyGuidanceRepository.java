package com.interviewforge.preparation;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyGuidanceRepository extends JpaRepository<CompanyGuidance, UUID> {
    List<CompanyGuidance> findAllByCompanyIdAndActiveTrueOrderByTitleAsc(UUID companyId);
    List<CompanyGuidance> findAllByOrderByCompanyIdAscTitleAsc();
}
