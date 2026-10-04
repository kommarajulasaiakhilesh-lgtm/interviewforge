package com.interviewforge.preparation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CompanyRoleRepository extends JpaRepository<CompanyRole,UUID> {
    List<CompanyRole> findAllByCompanyIdAndActiveTrueOrderByNameAsc(UUID companyId);
    List<CompanyRole> findAllByOrderByNameAsc();
}
