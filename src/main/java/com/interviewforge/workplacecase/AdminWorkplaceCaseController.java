package com.interviewforge.workplacecase;

import com.interviewforge.questionbank.QuestionBankDtos.PageResponse;
import com.interviewforge.workplacecase.WorkplaceCaseDtos.AdminCaseDetail;
import com.interviewforge.workplacecase.WorkplaceCaseDtos.AdminCaseInput;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/workplace-cases")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWorkplaceCaseController {
    private final WorkplaceCaseService service;
    public AdminWorkplaceCaseController(WorkplaceCaseService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCaseDetail create(@Valid @RequestBody AdminCaseInput request) { return service.create(request); }

    @GetMapping
    public PageResponse<AdminCaseDetail> browse(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) { return service.adminBrowse(page, size); }

    @GetMapping("/{caseId}")
    public AdminCaseDetail get(@PathVariable UUID caseId) { return service.adminGet(caseId); }

    @PutMapping("/{caseId}")
    public AdminCaseDetail update(@PathVariable UUID caseId, @Valid @RequestBody AdminCaseInput request) {
        return service.update(caseId, request);
    }

    @DeleteMapping("/{caseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(@PathVariable UUID caseId) { service.archive(caseId); }
}
