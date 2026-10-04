package com.interviewforge.preparation;

import com.interviewforge.preparation.PreparationDtos.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class PreparationController {
    private final PreparationService service;
    public PreparationController(PreparationService service){this.service=service;}
    @GetMapping("/companies") public List<CompanyResponse> companies(){return service.companies();}
    @GetMapping("/companies/{companyId}/roles") public List<RoleResponse> roles(@PathVariable UUID companyId){return service.companyRoles(companyId);}
    @GetMapping("/roles/{roleId}") public RoleDetail role(@PathVariable UUID roleId){return service.roleDetail(roleId);}
    @GetMapping("/preparation-sets") public List<SetSummary> sets(@RequestParam UUID roleId){return service.studentSets(roleId);}
    @GetMapping("/preparation-sets/{id}") public SetDetail set(@PathVariable UUID id){return service.studentSet(id);}

    @RestController
    @RequestMapping("/api/v1/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public static class AdminController {
        private final PreparationService service;
        public AdminController(PreparationService service){this.service=service;}
        @GetMapping("/companies") public List<CompanyResponse> companies(){return service.adminCompanies();}
        @PostMapping("/companies") @ResponseStatus(HttpStatus.CREATED)
        public CompanyResponse createCompany(@Valid @RequestBody CatalogInput in){return service.createCompany(in);}
        @PutMapping("/companies/{id}") public CompanyResponse updateCompany(@PathVariable UUID id,@Valid @RequestBody CatalogInput in){return service.updateCompany(id,in);}
        @GetMapping("/roles") public List<RoleResponse> roles(){return service.adminRoles();}
        @GetMapping("/roles/{id}") public RoleDetail role(@PathVariable UUID id){return service.adminRoleDetail(id);}
        @PostMapping("/roles") @ResponseStatus(HttpStatus.CREATED)
        public RoleResponse createRole(@Valid @RequestBody RoleInput in){return service.createRole(in);}
        @PutMapping("/roles/{id}") public RoleResponse updateRole(@PathVariable UUID id,@Valid @RequestBody RoleInput in){return service.updateRole(id,in);}
        @PutMapping("/roles/{id}/skills") @ResponseStatus(HttpStatus.NO_CONTENT)
        public void setRoleSkills(@PathVariable UUID id,@Valid @RequestBody RoleSkillsInput in){service.replaceRoleSkills(id,in);}
        @GetMapping("/skills") public List<SkillResponse> skills(){return service.adminSkills();}
        @PostMapping("/skills") @ResponseStatus(HttpStatus.CREATED)
        public SkillResponse createSkill(@Valid @RequestBody CatalogInput in){return service.createSkill(in);}
        @PutMapping("/skills/{id}") public SkillResponse updateSkill(@PathVariable UUID id,@Valid @RequestBody CatalogInput in){return service.updateSkill(id,in);}
        @PutMapping("/skills/{id}/topics") @ResponseStatus(HttpStatus.NO_CONTENT)
        public void setSkillTopics(@PathVariable UUID id,@Valid @RequestBody SkillTopicsInput in){service.replaceSkillTopics(id,in);}
        @GetMapping("/skills/{id}/topics") public List<TopicLink> skillTopics(@PathVariable UUID id){return service.adminSkillTopics(id);}
        @GetMapping("/preparation-sets") public List<AdminSetSummary> sets(){return service.adminSets();}
        @GetMapping("/preparation-sets/{id}") public AdminSetDetail set(@PathVariable UUID id){return service.adminSetById(id);}
        @PostMapping("/preparation-sets") @ResponseStatus(HttpStatus.CREATED)
        public AdminSetDetail createSet(@Valid @RequestBody SetInput in){return service.createSet(in);}
        @PutMapping("/preparation-sets/{id}") public AdminSetDetail updateSet(@PathVariable UUID id,@Valid @RequestBody SetInput in){return service.updateSet(id,in);}
        @DeleteMapping("/preparation-sets/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
        public void archiveSet(@PathVariable UUID id){service.archiveSet(id);}
    }
}
