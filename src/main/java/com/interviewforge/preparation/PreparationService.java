package com.interviewforge.preparation;

import com.interviewforge.preparation.PreparationDtos.*;
import com.interviewforge.questionbank.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class PreparationService {
    private final CompanyRepository companies;
    private final CompanyRoleRepository roles;
    private final SkillRepository skills;
    private final RoleSkillMappingRepository roleSkills;
    private final SkillTopicMappingRepository skillTopics;
    private final PreparationSetRepository sets;
    private final PreparationSetQuestionRepository setQuestions;
    private final TopicRepository topics;
    private final QuestionRepository questions;
    private final QuestionBankService questionBank;

    public PreparationService(CompanyRepository companies, CompanyRoleRepository roles, SkillRepository skills,
            RoleSkillMappingRepository roleSkills, SkillTopicMappingRepository skillTopics,
            PreparationSetRepository sets, PreparationSetQuestionRepository setQuestions, TopicRepository topics,
            QuestionRepository questions, QuestionBankService questionBank) {
        this.companies=companies;this.roles=roles;this.skills=skills;this.roleSkills=roleSkills;this.skillTopics=skillTopics;
        this.sets=sets;this.setQuestions=setQuestions;this.topics=topics;this.questions=questions;this.questionBank=questionBank;
    }

    @Transactional(readOnly=true)
    public List<CompanyResponse> companies(){return companies.findAllByActiveTrueOrderByNameAsc().stream().map(this::companyDto).toList();}
    @Transactional(readOnly=true)
    public List<RoleResponse> companyRoles(UUID companyId){Company c=activeCompany(companyId);return roles.findAllByCompanyIdAndActiveTrueOrderByNameAsc(c.getId()).stream().map(this::roleDto).toList();}
    @Transactional(readOnly=true)
    public RoleDetail roleDetail(UUID id){return roleDetail(id,false);}
    @Transactional(readOnly=true)
    public RoleDetail adminRoleDetail(UUID id){return roleDetail(id,true);}
    private RoleDetail roleDetail(UUID id,boolean includeInactive){CompanyRole role=includeInactive?roles.findById(id).orElseThrow(()->missing("Role",id)):activeRole(id);List<SkillLink> links=roleSkills.findAllByRoleIdOrderByImportanceDesc(id).stream()
            .map(m->{Skill s=skills.findById(m.getSkillId()).filter(x->includeInactive||x.isActive()).orElse(null);if(s==null)return null;
                List<TopicLink> topicLinks=skillTopics.findAllBySkillId(s.getId()).stream().map(tm->topics.findById(tm.getTopicId())
                        .filter(t->includeInactive||t.isActive()).map(t->new TopicLink(t.getId(),t.getName(),tm.getRelevance())).orElse(null)).filter(Objects::nonNull).toList();
                return new SkillLink(s.getId(),s.getName(),m.getImportance(),topicLinks);}).filter(Objects::nonNull).toList();
        return new RoleDetail(roleDto(role),links);}
    @Transactional(readOnly=true)
    public List<TopicLink> adminSkillTopics(UUID skillId){skills.findById(skillId).orElseThrow(()->missing("Skill",skillId));return skillTopics.findAllBySkillId(skillId).stream().map(m->topics.findById(m.getTopicId())
            .map(t->new TopicLink(t.getId(),t.getName(),m.getRelevance())).orElse(null)).filter(Objects::nonNull).toList();}

    public CompanyResponse createCompany(CatalogInput in){return companyDto(companies.save(new Company(in.name().trim(),slug(in.slug(),in.name()),in.description())));}
    public CompanyResponse updateCompany(UUID id,CatalogInput in){Company c=companies.findById(id).orElseThrow(()->missing("Company",id));c.update(in.name().trim(),slug(in.slug(),in.name()),in.description(),in.active()==null?c.isActive():in.active());return companyDto(c);}
    public RoleResponse createRole(RoleInput in){Company c=companies.findById(in.companyId()).orElseThrow(()->missing("Company",in.companyId()));return roleDto(roles.save(new CompanyRole(c.getId(),in.name().trim(),slug(in.slug(),in.name()),in.description())));}
    public RoleResponse updateRole(UUID id,RoleInput in){CompanyRole r=roles.findById(id).orElseThrow(()->missing("Role",id));companies.findById(in.companyId()).orElseThrow(()->missing("Company",in.companyId()));r.update(in.companyId(),in.name().trim(),slug(in.slug(),in.name()),in.description(),in.active()==null?r.isActive():in.active());return roleDto(r);}
    public SkillResponse createSkill(CatalogInput in){return skillDto(skills.save(new Skill(in.name().trim(),slug(in.slug(),in.name()),in.description())));}
    public SkillResponse updateSkill(UUID id,CatalogInput in){Skill s=skills.findById(id).orElseThrow(()->missing("Skill",id));s.update(in.name().trim(),slug(in.slug(),in.name()),in.description(),in.active()==null?s.isActive():in.active());return skillDto(s);}

    public void replaceRoleSkills(UUID roleId,RoleSkillsInput input){roles.findById(roleId).orElseThrow(()->missing("Role",roleId));List<SkillWeight> links=input.mappings();
        ensureUnique(links.stream().map(SkillWeight::skillId).toList(),"skill");roleSkills.deleteAllByRoleId(roleId);
        for(SkillWeight link:links){skills.findById(link.skillId()).orElseThrow(()->missing("Skill",link.skillId()));roleSkills.save(new RoleSkillMapping(roleId,link.skillId(),link.importance()));}}
    public void replaceSkillTopics(UUID skillId,SkillTopicsInput input){skills.findById(skillId).orElseThrow(()->missing("Skill",skillId));List<TopicWeight> links=input.mappings();
        ensureUnique(links.stream().map(TopicWeight::topicId).toList(),"topic");skillTopics.deleteAllBySkillId(skillId);
        for(TopicWeight link:links){topics.findById(link.topicId()).orElseThrow(()->missing("Topic",link.topicId()));skillTopics.save(new SkillTopicMapping(skillId,link.topicId(),link.relevance()));}}

    public AdminSetDetail createSet(SetInput in){CompanyRole role=roles.findById(in.roleId()).orElseThrow(()->missing("Role",in.roleId()));
        validateQuestions(in.questionIds());PreparationSet set=sets.save(new PreparationSet(role.getId(),in.title().trim(),in.description(),Boolean.TRUE.equals(in.published())));
        saveSetQuestions(set.getId(),in.questionIds());return adminSet(set);}
    public AdminSetDetail updateSet(UUID id,SetInput in){PreparationSet set=sets.findById(id).orElseThrow(()->missing("Preparation set",id));
        CompanyRole role=roles.findById(in.roleId()).orElseThrow(()->missing("Role",in.roleId()));validateQuestions(in.questionIds());
        set.update(role.getId(),in.title().trim(),in.description(),Boolean.TRUE.equals(in.published()));setQuestions.deleteAllBySetId(id);saveSetQuestions(id,in.questionIds());return adminSet(set);}
    public void archiveSet(UUID id){PreparationSet set=sets.findById(id).orElseThrow(()->missing("Preparation set",id));set.archive();}

    @Transactional(readOnly=true)
    public List<CompanyResponse> adminCompanies(){return companies.findAllByOrderByNameAsc().stream().map(this::companyDto).toList();}
    @Transactional(readOnly=true)
    public List<RoleResponse> adminRoles(){return roles.findAllByOrderByNameAsc().stream().map(this::roleDto).toList();}
    @Transactional(readOnly=true)
    public List<SkillResponse> adminSkills(){return skills.findAllByOrderByNameAsc().stream().map(this::skillDto).toList();}
    @Transactional(readOnly=true)
    public List<AdminSetSummary> adminSets(){return sets.findAllByOrderByTitleAsc().stream().map(s->new AdminSetSummary(s.getId(),s.getRoleId(),s.getTitle(),s.getDescription(),s.isPublished(),s.isArchived(),setQuestions.findAllBySetIdOrderByPosition(s.getId()).size())).toList();}
    @Transactional(readOnly=true)
    public AdminSetDetail adminSetById(UUID id){return adminSet(sets.findById(id).orElseThrow(()->missing("Preparation set",id)));}

    @Transactional(readOnly=true)
    public List<SetSummary> studentSets(UUID roleId){CompanyRole role=activeRole(roleId);activeCompany(role.getCompanyId());return sets.findAllByRoleIdAndPublishedTrueAndArchivedFalseOrderByTitleAsc(roleId).stream().map(s->setDto(s,role)).toList();}
    @Transactional(readOnly=true)
    public SetDetail studentSet(UUID id){PreparationSet set=sets.findById(id).filter(s->s.isPublished()&&!s.isArchived()).orElseThrow(()->missing("Preparation set",id));CompanyRole role=activeRole(set.getRoleId());activeCompany(role.getCompanyId());
        List<QuestionBankDtos.QuestionSummary> qs=setQuestions.findAllBySetIdOrderByPosition(id).stream().map(PreparationSetQuestion::getQuestionId)
                .map(qid->{try{return questionBank.getPublishedQuestion(qid);}catch(ResourceNotFoundException ignored){return null;}}).filter(Objects::nonNull).toList();
        return new SetDetail(setDto(set,role),qs);}

    private void validateQuestions(List<UUID> ids){ensureUnique(ids,"question");for(UUID id:ids)questionBank.getPublishedQuestion(id);}
    private void saveSetQuestions(UUID setId,List<UUID> ids){int pos=1;for(UUID id:ids)setQuestions.save(new PreparationSetQuestion(setId,id,pos++));}
    private AdminSetDetail adminSet(PreparationSet set){return new AdminSetDetail(new AdminSetSummary(set.getId(),set.getRoleId(),set.getTitle(),set.getDescription(),set.isPublished(),set.isArchived(),setQuestions.findAllBySetIdOrderByPosition(set.getId()).size()),setQuestions.findAllBySetIdOrderByPosition(set.getId()).stream().map(PreparationSetQuestion::getQuestionId).toList());}
    private SetSummary setDto(PreparationSet set,CompanyRole role){Company c=companies.findById(role.getCompanyId()).orElseThrow(()->missing("Company",role.getCompanyId()));return new SetSummary(set.getId(),role.getId(),role.getName(),c.getName(),set.getTitle(),set.getDescription(),set.isPublished(),setQuestions.findAllBySetIdOrderByPosition(set.getId()).size());}
    private Company activeCompany(UUID id){return companies.findById(id).filter(Company::isActive).orElseThrow(()->missing("Company",id));}
    private CompanyRole activeRole(UUID id){CompanyRole r=roles.findById(id).filter(CompanyRole::isActive).orElseThrow(()->missing("Role",id));activeCompany(r.getCompanyId());return r;}
    private CompanyResponse companyDto(Company c){return new CompanyResponse(c.getId(),c.getName(),c.getSlug(),c.getDescription(),c.isActive());}
    private RoleResponse roleDto(CompanyRole r){Company c=companies.findById(r.getCompanyId()).orElseThrow(()->missing("Company",r.getCompanyId()));return new RoleResponse(r.getId(),c.getId(),c.getName(),r.getName(),r.getSlug(),r.getDescription(),r.isActive());}
    private SkillResponse skillDto(Skill s){return new SkillResponse(s.getId(),s.getName(),s.getSlug(),s.getDescription(),s.isActive());}
    private String slug(String provided,String name){String source=StringUtils.hasText(provided)?provided:name;return source.trim().toLowerCase().replaceAll("[^a-z0-9]+","-").replaceAll("^-|-$","");}
    private void ensureUnique(List<UUID> ids,String kind){if(ids==null)return;if(ids.stream().distinct().count()!=ids.size())throw new IllegalArgumentException("Duplicate "+kind+" IDs are not allowed.");}
    private ResourceNotFoundException missing(String kind,Object id){return new ResourceNotFoundException(kind,id);}
}
