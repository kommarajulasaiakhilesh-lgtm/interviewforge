package com.interviewforge.preparation;

import com.interviewforge.questionbank.QuestionBankDtos.QuestionSummary;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public final class PreparationDtos {
    private PreparationDtos() { }
    public record CatalogInput(@NotBlank @Size(max=120) String name,@Size(max=140) String slug,
            @Size(max=1000) String description,Boolean active) { }
    public record RoleInput(@NotNull UUID companyId,@NotBlank @Size(max=120) String name,
            @Size(max=140) String slug,@Size(max=1000) String description,Boolean active) { }
    public record CompanyResponse(UUID id,String name,String slug,String description,boolean active) { }
    public record RoleResponse(UUID id,UUID companyId,String companyName,String name,String slug,String description,boolean active) { }
    public record SkillResponse(UUID id,String name,String slug,String description,boolean active) { }
    public record TopicWeight(@NotNull UUID topicId,@Min(1) @Max(5) int relevance) { }
    public record SkillWeight(@NotNull UUID skillId,@Min(1) @Max(5) int importance) { }
    public record RoleSkillsInput(@NotNull @Size(max=50) List<@Valid SkillWeight> mappings) { }
    public record SkillTopicsInput(@NotNull @Size(max=50) List<@Valid TopicWeight> mappings) { }
    public record TopicLink(UUID topicId,String topicName,int relevance) { }
    public record SkillLink(UUID skillId,String skillName,int importance,List<TopicLink> topics) { }
    public record RoleDetail(RoleResponse role,List<SkillLink> skills) { }
    public record SetInput(@NotNull UUID roleId,@NotBlank @Size(max=160) String title,
            @Size(max=1000) String description,Boolean published,
            @NotNull @Size(min=1,max=50) List<@NotNull UUID> questionIds) { }
    public record SetSummary(UUID id,UUID roleId,String roleName,String companyName,String title,
            String description,boolean published,int questionCount) { }
    public record AdminSetSummary(UUID id,UUID roleId,String title,String description,boolean published,boolean archived,int questionCount) { }
    public record SetDetail(SetSummary set,List<QuestionSummary> questions) { }
    public record AdminSetDetail(AdminSetSummary set,List<UUID> questionIds) { }
}
