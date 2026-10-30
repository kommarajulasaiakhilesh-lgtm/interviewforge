package com.interviewforge.preparation;

import com.interviewforge.preparation.CompanyGuidanceDtos.GuidanceInput;
import com.interviewforge.preparation.CompanyGuidanceDtos.GuidanceResponse;
import com.interviewforge.questionbank.ResourceNotFoundException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyGuidanceService {
    private final CompanyGuidanceRepository guidance;
    private final CompanyRepository companies;
    private final CompanyRoleRepository roles;
    private final Clock clock;

    public CompanyGuidanceService(CompanyGuidanceRepository guidance, CompanyRepository companies,
            CompanyRoleRepository roles) {
        this(guidance, companies, roles, Clock.systemUTC());
    }

    CompanyGuidanceService(CompanyGuidanceRepository guidance, CompanyRepository companies,
            CompanyRoleRepository roles, Clock clock) {
        this.guidance = guidance; this.companies = companies; this.roles = roles; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<GuidanceResponse> companyGuidance(UUID companyId, UUID roleId) {
        Company company = activeCompany(companyId);
        if (roleId != null && !roles.findAllByCompanyIdAndActiveTrueOrderByNameAsc(companyId)
                .stream().anyMatch(role -> role.getId().equals(roleId))) {
            throw missing("Role", roleId);
        }
        return guidance.findAllByCompanyIdAndActiveTrueOrderByTitleAsc(companyId).stream()
                .filter(item -> item.getRoleId() == null || item.getRoleId().equals(roleId))
                .map(item -> response(item, company, item.getRoleId() == null ? null : roles.findById(item.getRoleId()).orElse(null)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GuidanceResponse> adminGuidance() {
        return guidance.findAllByOrderByCompanyIdAscTitleAsc().stream().map(item -> response(item,
                companies.findById(item.getCompanyId()).orElseThrow(() -> missing("Company", item.getCompanyId())),
                item.getRoleId() == null ? null : roles.findById(item.getRoleId()).orElse(null))).toList();
    }

    public GuidanceResponse create(GuidanceInput input) {
        Company company = companies.findById(input.companyId()).orElseThrow(() -> missing("Company", input.companyId()));
        CompanyRole role = validateRole(company, input.roleId());
        validateDates(input);
        CompanyGuidance item = guidance.save(new CompanyGuidance(company.getId(), role == null ? null : role.getId(),
                input.title().trim(), input.summary().trim(), input.resourceType(), input.sourceType(),
                input.sourceLabel().trim(), input.sourceUrl().trim(), input.publishedOn(), input.lastVerifiedOn(), input.validUntil()));
        return response(item, company, role);
    }

    public GuidanceResponse update(UUID id, GuidanceInput input) {
        CompanyGuidance item = guidance.findById(id).orElseThrow(() -> missing("Company guidance", id));
        Company company = companies.findById(input.companyId()).orElseThrow(() -> missing("Company", input.companyId()));
        CompanyRole role = validateRole(company, input.roleId());
        validateDates(input);
        item.update(company.getId(), role == null ? null : role.getId(), input.title().trim(), input.summary().trim(),
                input.resourceType(), input.sourceType(), input.sourceLabel().trim(), input.sourceUrl().trim(),
                input.publishedOn(), input.lastVerifiedOn(), input.validUntil(), input.active() == null ? item.isActive() : input.active());
        return response(item, company, role);
    }

    public void archive(UUID id) {
        CompanyGuidance item = guidance.findById(id).orElseThrow(() -> missing("Company guidance", id));
        item.update(item.getCompanyId(), item.getRoleId(), item.getTitle(), item.getSummary(), item.getResourceType(),
                item.getSourceType(), item.getSourceLabel(), item.getSourceUrl(), item.getPublishedOn(),
                item.getLastVerifiedOn(), item.getValidUntil(), false);
    }

    private GuidanceResponse response(CompanyGuidance item, Company company, CompanyRole role) {
        LocalDate today = LocalDate.now(clock);
        String freshness = freshness(item.getLastVerifiedOn(), item.getValidUntil(), today);
        return new GuidanceResponse(item.getId(), item.getCompanyId(), company.getName(), item.getRoleId(),
                role == null ? null : role.getName(), item.getTitle(), item.getSummary(), item.getResourceType(),
                item.getSourceType(), item.getSourceLabel(), item.getSourceUrl(), item.getPublishedOn(),
                item.getLastVerifiedOn(), item.getValidUntil(), freshness, item.isActive());
    }

    static String freshness(LocalDate lastVerifiedOn, LocalDate validUntil, LocalDate today) {
        if (validUntil != null && validUntil.isBefore(today)) return "EXPIRED";
        return ChronoUnit.DAYS.between(lastVerifiedOn, today) <= 90 ? "RECENTLY_VERIFIED" : "REVIEW_DUE";
    }

    private CompanyRole validateRole(Company company, UUID roleId) {
        if (roleId == null) return null;
        CompanyRole role = roles.findById(roleId).orElseThrow(() -> missing("Role", roleId));
        if (!role.getCompanyId().equals(company.getId())) throw new IllegalArgumentException("Role must belong to the selected company.");
        return role;
    }

    private void validateDates(GuidanceInput input) {
        if (input.publishedOn() != null && input.publishedOn().isAfter(LocalDate.now(clock)))
            throw new IllegalArgumentException("Publication date cannot be in the future.");
        if (input.lastVerifiedOn().isAfter(LocalDate.now(clock)))
            throw new IllegalArgumentException("Verification date cannot be in the future.");
        if (input.publishedOn() != null && input.publishedOn().isAfter(input.lastVerifiedOn()))
            throw new IllegalArgumentException("Publication date cannot be after the verification date.");
        if (input.validUntil() != null && input.validUntil().isBefore(input.lastVerifiedOn()))
            throw new IllegalArgumentException("Validity date cannot be before the verification date.");
    }

    private Company activeCompany(UUID id) {
        return companies.findById(id).filter(Company::isActive).orElseThrow(() -> missing("Company", id));
    }
    private ResourceNotFoundException missing(String kind, Object id) { return new ResourceNotFoundException(kind, id); }
}
