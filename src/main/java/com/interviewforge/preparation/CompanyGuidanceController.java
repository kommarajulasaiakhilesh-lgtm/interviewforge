package com.interviewforge.preparation;

import com.interviewforge.preparation.CompanyGuidanceDtos.GuidanceInput;
import com.interviewforge.preparation.CompanyGuidanceDtos.GuidanceResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CompanyGuidanceController {
    private final CompanyGuidanceService service;
    public CompanyGuidanceController(CompanyGuidanceService service) { this.service = service; }

    @GetMapping("/companies/{companyId}/guidance")
    public List<GuidanceResponse> companyGuidance(@PathVariable UUID companyId, @RequestParam(required = false) UUID roleId) {
        return service.companyGuidance(companyId, roleId);
    }

    @RestController
    @RequestMapping("/api/v1/admin/company-guidance")
    @PreAuthorize("hasRole('ADMIN')")
    public static class AdminController {
        private final CompanyGuidanceService service;
        public AdminController(CompanyGuidanceService service) { this.service = service; }
        @GetMapping public List<GuidanceResponse> list() { return service.adminGuidance(); }
        @PostMapping @ResponseStatus(HttpStatus.CREATED)
        public GuidanceResponse create(@Valid @RequestBody GuidanceInput input) { return service.create(input); }
        @PutMapping("/{id}")
        public GuidanceResponse update(@PathVariable UUID id, @Valid @RequestBody GuidanceInput input) { return service.update(id, input); }
        @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
        public void archive(@PathVariable UUID id) { service.archive(id); }
    }
}
