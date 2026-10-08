package com.interviewforge.preparation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface CompanyRoleRepository extends JpaRepository<CompanyRole,UUID> {
    List<CompanyRole> findAllByCompanyIdAndActiveTrueOrderByNameAsc(UUID companyId);
    List<CompanyRole> findAllByOrderByNameAsc();
    @Query("select case when count(r) > 0 then true else false end from CompanyRole r, Company c where r.id = :id and r.active = true and r.companyId = c.id and c.active = true")
    boolean existsActiveRole(@Param("id") UUID id);
}
