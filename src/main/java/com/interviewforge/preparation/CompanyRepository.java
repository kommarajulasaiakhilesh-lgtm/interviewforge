package com.interviewforge.preparation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CompanyRepository extends JpaRepository<Company,UUID> {
    List<Company> findAllByActiveTrueOrderByNameAsc();
    List<Company> findAllByOrderByNameAsc();
}
