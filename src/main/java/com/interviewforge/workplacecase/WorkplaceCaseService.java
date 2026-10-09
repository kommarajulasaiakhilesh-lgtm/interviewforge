package com.interviewforge.workplacecase;

import com.interviewforge.preparation.Company;
import com.interviewforge.preparation.CompanyRepository;
import com.interviewforge.preparation.CompanyRole;
import com.interviewforge.preparation.CompanyRoleRepository;
import com.interviewforge.questionbank.Difficulty;
import com.interviewforge.questionbank.ResourceNotFoundException;
import com.interviewforge.questionbank.Topic;
import com.interviewforge.questionbank.TopicRepository;
import com.interviewforge.questionbank.QuestionBankDtos.PageResponse;
import com.interviewforge.workplacecase.WorkplaceCaseDtos.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional
public class WorkplaceCaseService {
    private static final Set<String> SOURCE_TYPES = Set.of("ORIGINAL", "OFFICIAL", "COMMUNITY_REPORTED", "EDITORIAL", "UNKNOWN");
    private final WorkplaceCaseRepository cases;
    private final WorkplaceCaseNodeRepository nodes;
    private final WorkplaceCaseChoiceRepository choices;
    private final WorkplaceCaseSessionRepository sessions;
    private final WorkplaceCaseDecisionRepository decisions;
    private final CompanyRoleRepository roles;
    private final CompanyRepository companies;
    private final TopicRepository topics;
    private final ObjectMapper mapper;

    public WorkplaceCaseService(WorkplaceCaseRepository cases, WorkplaceCaseNodeRepository nodes,
            WorkplaceCaseChoiceRepository choices, WorkplaceCaseSessionRepository sessions,
            WorkplaceCaseDecisionRepository decisions, CompanyRoleRepository roles, CompanyRepository companies,
            TopicRepository topics, ObjectMapper mapper) {
        this.cases = cases; this.nodes = nodes; this.choices = choices; this.sessions = sessions; this.decisions = decisions;
        this.roles = roles; this.companies = companies; this.topics = topics; this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<CaseSummary> browse(UUID roleId, UUID topicId, Difficulty difficulty, int page, int size) {
        validatePage(page, size);
        Page<WorkplaceCase> result = cases.findVisible(roleId, topicId, difficulty,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "title")));
        return page(result, result.getContent().stream().map(this::summary).toList());
    }

    @Transactional(readOnly = true)
    public CaseSummary getPublished(UUID id) {
        WorkplaceCase workplaceCase = visibleCase(id);
        return summary(workplaceCase);
    }

    public AdminCaseDetail create(AdminCaseInput input) {
        validateGraph(input);
        ensureActiveReferences(input.roleId(), input.topicId());
        String slug = slug(input.slug(), input.title());
        if (cases.existsBySlug(slug)) throw new DuplicateWorkplaceCaseSlugException();
        validateSource(input.sourceType(), input.sourceUrl());
        WorkplaceCase workplaceCase = cases.saveAndFlush(new WorkplaceCase(input.title().trim(), slug, input.description(),
                input.roleId(), input.topicId(), input.difficulty(), input.estimatedMinutes(), input.scenarioIntro().trim(),
                input.learningObjective(), input.sourceType(), input.sourceLabel(), input.sourceUrl(), input.sourceVerifiedAt(),
                Boolean.TRUE.equals(input.published())));
        replaceGraph(workplaceCase.getId(), input.nodes());
        return adminDetail(workplaceCase);
    }

    public AdminCaseDetail update(UUID id, AdminCaseInput input) {
        validateGraph(input);
        ensureActiveReferences(input.roleId(), input.topicId());
        validateSource(input.sourceType(), input.sourceUrl());
        WorkplaceCase workplaceCase = cases.findById(id).orElseThrow(() -> new ResourceNotFoundException("Workplace case", id));
        String slug = slug(input.slug(), input.title());
        if (cases.existsBySlugAndIdNot(slug, id)) throw new DuplicateWorkplaceCaseSlugException();
        workplaceCase.update(input.title().trim(), slug, input.description(), input.roleId(), input.topicId(), input.difficulty(),
                input.estimatedMinutes(), input.scenarioIntro().trim(), input.learningObjective(), input.sourceType(), input.sourceLabel(),
                input.sourceUrl(), input.sourceVerifiedAt(), Boolean.TRUE.equals(input.published()));
        cases.saveAndFlush(workplaceCase);
        replaceGraph(id, input.nodes());
        return adminDetail(workplaceCase);
    }

    public void archive(UUID id) {
        WorkplaceCase workplaceCase = cases.findById(id).orElseThrow(() -> new ResourceNotFoundException("Workplace case", id));
        workplaceCase.archive();
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminCaseDetail> adminBrowse(int page, int size) {
        validatePage(page, size);
        Page<WorkplaceCase> result = cases.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return page(result, result.getContent().stream().map(this::adminDetail).toList());
    }

    @Transactional(readOnly = true)
    public AdminCaseDetail adminGet(UUID id) {
        return adminDetail(cases.findById(id).orElseThrow(() -> new ResourceNotFoundException("Workplace case", id)));
    }

    public CreateSessionResponse start(UUID userId, UUID caseId) {
        WorkplaceCase workplaceCase = visibleCase(caseId);
        CaseGraphSnapshot graph = graphSnapshot(workplaceCase);
        String json = writeGraph(graph);
        NodeSnapshot first = graph.nodes().stream().filter(n -> n.nodeType() == CaseNodeType.START).findFirst().orElseThrow();
        WorkplaceCaseSession session = sessions.save(new WorkplaceCaseSession(userId, workplaceCase,
                role(workplaceCase.getRoleId()).getName(), json, first.nodeKey()));
        return new CreateSessionResponse(summary(session), graph.scenarioIntro(), graph.learningObjective(),
                workplaceCase.getSourceType(), workplaceCase.getSourceLabel(), workplaceCase.getSourceUrl(),
                workplaceCase.getSourceVerifiedAt(), prompt(first));
    }

    public DecisionResponse submit(UUID userId, UUID sessionId, DecisionRequest request) {
        WorkplaceCaseSession session = sessions.findOwnedForUpdate(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Workplace case session", sessionId));
        if (session.getStatus() != WorkplaceCaseStatus.IN_PROGRESS) throw new IllegalArgumentException("This workplace case is already complete.");
        if (!session.getCurrentNodeKey().equals(request.nodeKey())) throw new IllegalArgumentException("The decision must match the current case step.");
        CaseGraphSnapshot graph = readGraph(session.getGraphSnapshot());
        NodeSnapshot node = findNode(graph, request.nodeKey());
        if (node.nodeType() == CaseNodeType.OUTCOME) throw new IllegalArgumentException("This case has reached its outcome.");
        ChoiceSnapshot choice = node.choices().stream().filter(c -> c.choiceKey().equals(request.choiceKey())).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Choice in current case step", request.choiceKey()));
        NodeSnapshot next = findNode(graph, choice.nextNodeKey());
        boolean completed = next.nodeType() == CaseNodeType.OUTCOME;
        int position = session.getDecisionCount() + 1;
        WorkplaceCaseDecision savedDecision = decisions.save(new WorkplaceCaseDecision(sessionId, position, node.nodeKey(), choice));
        session.recordDecision(next.nodeKey(), choice.qualityPoints(), completed);
        DecisionReview review = new DecisionReview(position, node.heading(), choice.choiceLabel(), choice.choiceText(),
                choice.decisionQuality(), choice.qualityPoints(), choice.explanation(), choice.whenAppropriate(),
                choice.tradeoffSummary(), choice.misconceptionLabel(), choice.nextNodeKey(), savedDecision.getAnsweredAt(),
                choiceFeedback(node, choice.choiceKey()));
        return new DecisionResponse(summary(session), review, prompt(next));
    }

    @Transactional(readOnly = true)
    public SessionDetail detail(UUID userId, UUID sessionId) {
        WorkplaceCaseSession session = owned(userId, sessionId);
        CaseGraphSnapshot graph = readGraph(session.getGraphSnapshot());
        NodeSnapshot current = findNode(graph, session.getCurrentNodeKey());
        List<DecisionReview> history = decisions.findAllBySessionIdOrderByPosition(sessionId).stream().map(d ->
                new DecisionReview(d.getPosition(), findNode(graph, d.getNodeKey()).heading(), d.getChoiceLabel(), d.getChoiceText(),
                        d.getDecisionQuality(), d.getQualityPoints(), d.getExplanation(), d.getWhenAppropriate(),
                        d.getTradeoffSummary(), d.getMisconceptionLabel(), d.getNextNodeKey(), d.getAnsweredAt(),
                        choiceFeedback(findNode(graph, d.getNodeKey()), d.getChoiceKey()))).toList();
        return new SessionDetail(summary(session), graph.scenarioIntro(), graph.learningObjective(), prompt(current), history);
    }

    @Transactional(readOnly = true)
    public PageResponse<SessionSummary> history(UUID userId, int page, int size) {
        validatePage(page, size);
        Page<WorkplaceCaseSession> result = sessions.findAllByUserIdOrderByStartedAtDesc(userId, PageRequest.of(page, size));
        return page(result, result.getContent().stream().map(this::summary).toList());
    }

    private void replaceGraph(UUID caseId, List<NodeInput> graphNodes) {
        choices.deleteForCase(caseId);
        nodes.deleteForCase(caseId);
        List<WorkplaceCaseNode> persistedNodes = new ArrayList<>();
        for (int i = 0; i < graphNodes.size(); i++) {
            NodeInput node = graphNodes.get(i);
            persistedNodes.add(new WorkplaceCaseNode(caseId, node.nodeKey(), node.nodeType(), i + 1,
                    node.heading().trim(), node.situationText().trim(), node.lessonText()));
        }
        nodes.saveAllAndFlush(persistedNodes);
        List<WorkplaceCaseChoice> persistedChoices = new ArrayList<>();
        for (NodeInput node : graphNodes) {
            if (node.choices() == null) continue;
            for (int i = 0; i < node.choices().size(); i++) {
                ChoiceInput choice = node.choices().get(i);
                persistedChoices.add(new WorkplaceCaseChoice(caseId, node.nodeKey(), i + 1, choice, points(choice.decisionQuality())));
            }
        }
        choices.saveAll(persistedChoices);
    }

    private AdminCaseDetail adminDetail(WorkplaceCase workplaceCase) {
        Map<String, List<ChoiceInput>> byNode = choices.findAllByCaseIdOrderByNodeKeyAscPositionAsc(workplaceCase.getId()).stream()
                .collect(Collectors.groupingBy(WorkplaceCaseChoice::getNodeKey, HashMap::new,
                        Collectors.mapping(c -> new ChoiceInput(c.getChoiceKey(), c.getChoiceLabel(), c.getChoiceText(), c.getDecisionQuality(),
                                c.getExplanation(), c.getWhenAppropriate(), c.getTradeoffSummary(), c.getMisconceptionLabel(), c.getNextNodeKey()),
                                Collectors.toList())));
        List<NodeInput> nodeInputs = nodes.findAllByCaseIdOrderByPosition(workplaceCase.getId()).stream()
                .map(n -> new NodeInput(n.getNodeKey(), n.getNodeType(), n.getHeading(), n.getSituationText(), n.getLessonText(),
                        byNode.getOrDefault(n.getNodeKey(), List.of()))).toList();
        return new AdminCaseDetail(summary(workplaceCase), workplaceCase.getScenarioIntro(), workplaceCase.isPublished(), workplaceCase.isArchived(),
                nodeInputs, workplaceCase.getCreatedAt(), workplaceCase.getUpdatedAt());
    }

    private CaseGraphSnapshot graphSnapshot(WorkplaceCase workplaceCase) {
        Map<String, List<ChoiceSnapshot>> byNode = choices.findAllByCaseIdOrderByNodeKeyAscPositionAsc(workplaceCase.getId()).stream()
                .collect(Collectors.groupingBy(WorkplaceCaseChoice::getNodeKey, HashMap::new,
                        Collectors.mapping(c -> new ChoiceSnapshot(c.getChoiceKey(), c.getChoiceLabel(), c.getChoiceText(),
                                c.getDecisionQuality(), c.getQualityPoints(), c.getExplanation(), c.getWhenAppropriate(),
                                c.getTradeoffSummary(), c.getMisconceptionLabel(), c.getNextNodeKey()), Collectors.toList())));
        List<NodeSnapshot> nodeSnapshots = nodes.findAllByCaseIdOrderByPosition(workplaceCase.getId()).stream()
                .map(n -> new NodeSnapshot(n.getNodeKey(), n.getNodeType(), n.getHeading(), n.getSituationText(), n.getLessonText(),
                        byNode.getOrDefault(n.getNodeKey(), List.of()))).toList();
        return new CaseGraphSnapshot(workplaceCase.getScenarioIntro(), workplaceCase.getLearningObjective(), nodeSnapshots);
    }

    private NodePrompt prompt(NodeSnapshot node) {
        List<ChoicePrompt> visibleChoices = node.choices().stream().map(c -> new ChoicePrompt(c.choiceKey(), c.choiceLabel(), c.choiceText())).toList();
        String lesson = node.nodeType() == CaseNodeType.OUTCOME ? node.lessonText() : null;
        return new NodePrompt(node.nodeKey(), node.nodeType(), node.heading(), node.situationText(), lesson, visibleChoices);
    }

    private List<ChoiceFeedback> choiceFeedback(NodeSnapshot node, String selectedChoiceKey) {
        return node.choices().stream().map(choice -> new ChoiceFeedback(choice.choiceKey(), choice.choiceLabel(), choice.choiceText(),
                choice.decisionQuality(), choice.qualityPoints(), choice.explanation(), choice.whenAppropriate(),
                choice.tradeoffSummary(), choice.misconceptionLabel(), choice.choiceKey().equals(selectedChoiceKey))).toList();
    }

    private CaseSummary summary(WorkplaceCase workplaceCase) {
        CompanyRole role = role(workplaceCase.getRoleId());
        Topic topic = topics.findById(workplaceCase.getTopicId()).orElseThrow(() -> new ResourceNotFoundException("Topic", workplaceCase.getTopicId()));
        return new CaseSummary(workplaceCase.getId(), workplaceCase.getTitle(), workplaceCase.getSlug(), workplaceCase.getDescription(),
                role.getId(), role.getName(), topic.getId(), topic.getName(), workplaceCase.getDifficulty(),
                workplaceCase.getEstimatedMinutes(), workplaceCase.getLearningObjective(), workplaceCase.getSourceType(),
                workplaceCase.getSourceLabel(), workplaceCase.getSourceUrl(), workplaceCase.getSourceVerifiedAt());
    }

    private SessionSummary summary(WorkplaceCaseSession session) {
        BigDecimal score = session.getDecisionCount() == 0 ? null : BigDecimal.valueOf(session.getTotalPoints() * 100L)
                .divide(BigDecimal.valueOf(session.getDecisionCount() * 2L), 2, RoundingMode.HALF_UP);
        return new SessionSummary(session.getId(), session.getCaseId(), session.getCaseTitle(), session.getRoleId(), session.getRoleName(),
                session.getDifficulty(), session.getStatus(), session.getDecisionCount(), session.getTotalPoints(), score,
                session.getStartedAt(), session.getCompletedAt());
    }

    private WorkplaceCase visibleCase(UUID id) {
        WorkplaceCase workplaceCase = cases.findById(id).filter(c -> c.isPublished() && !c.isArchived())
                .orElseThrow(() -> new ResourceNotFoundException("Workplace case", id));
        ensureActiveReferences(workplaceCase.getRoleId(), workplaceCase.getTopicId());
        return workplaceCase;
    }
    private WorkplaceCaseSession owned(UUID userId, UUID id) {
        return sessions.findByIdAndUserId(id, userId).orElseThrow(() -> new ResourceNotFoundException("Workplace case session", id));
    }
    private CompanyRole role(UUID id) {
        return roles.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }
    private void ensureActiveReferences(UUID roleId, UUID topicId) {
        CompanyRole role = roles.findById(roleId).filter(CompanyRole::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Active role", roleId));
        companies.findById(role.getCompanyId()).filter(Company::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Active role", roleId));
        topics.findById(topicId).filter(Topic::isActive).orElseThrow(() -> new ResourceNotFoundException("Active topic", topicId));
    }
    private void validateSource(String sourceType, String sourceUrl) {
        if (sourceType != null && !sourceType.isBlank() && !SOURCE_TYPES.contains(sourceType)) {
            throw new IllegalArgumentException("sourceType must be ORIGINAL, OFFICIAL, COMMUNITY_REPORTED, EDITORIAL, or UNKNOWN.");
        }
        if (StringUtils.hasText(sourceUrl) && !(sourceUrl.startsWith("https://") || sourceUrl.startsWith("http://"))) {
            throw new IllegalArgumentException("sourceUrl must use http or https.");
        }
    }
    private String slug(String provided, String title) {
        String source = StringUtils.hasText(provided) ? provided : title;
        String slug = source.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (slug.isBlank()) throw new IllegalArgumentException("A valid slug could not be derived from the case title.");
        return slug;
    }
    private int points(DecisionQuality quality) { return switch (quality) { case STRONG -> 2; case VIABLE -> 1; case RISKY -> 0; }; }
    private NodeSnapshot findNode(CaseGraphSnapshot graph, String nodeKey) {
        return graph.nodes().stream().filter(node -> node.nodeKey().equals(nodeKey)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Saved workplace case contains an invalid node reference."));
    }
    private CaseGraphSnapshot readGraph(String json) {
        try { return mapper.readValue(json, new TypeReference<>() { }); }
        catch (JacksonException exception) { throw new IllegalStateException("Unable to read saved workplace case.", exception); }
    }
    private String writeGraph(CaseGraphSnapshot graph) {
        try { return mapper.writeValueAsString(graph); }
        catch (JacksonException exception) { throw new IllegalStateException("Unable to save workplace case snapshot.", exception); }
    }
    private void validateGraph(AdminCaseInput input) {
        Map<String, NodeInput> byKey = new HashMap<>();
        long starts = input.nodes().stream().filter(n -> n.nodeType() == CaseNodeType.START).count();
        if (starts != 1 || input.nodes().getFirst().nodeType() != CaseNodeType.START) {
            throw new IllegalArgumentException("The first case step must be the only START node.");
        }
        for (NodeInput node : input.nodes()) {
            if (byKey.putIfAbsent(node.nodeKey(), node) != null) throw new IllegalArgumentException("Node keys must be unique.");
            List<ChoiceInput> nodeChoices = node.choices() == null ? List.of() : node.choices();
            if (node.nodeType() == CaseNodeType.OUTCOME) {
                if (!nodeChoices.isEmpty() || !StringUtils.hasText(node.lessonText())) throw new IllegalArgumentException("OUTCOME steps need lessonText and cannot have choices.");
            } else if (nodeChoices.size() < 2 || nodeChoices.size() > 6) {
                throw new IllegalArgumentException("START and DECISION steps need 2 to 6 choices.");
            }
            Set<String> choiceKeys = new HashSet<>();
            for (ChoiceInput choice : nodeChoices) {
                if (!choiceKeys.add(choice.choiceKey())) throw new IllegalArgumentException("Choice keys must be unique within a step.");
            }
        }
        for (NodeInput node : input.nodes()) {
            for (ChoiceInput choice : node.choices() == null ? List.<ChoiceInput>of() : node.choices()) {
                if (!byKey.containsKey(choice.nextNodeKey())) throw new IllegalArgumentException("Every choice must point to an existing next step.");
                if (choice.nextNodeKey().equals(node.nodeKey())) throw new IllegalArgumentException("A case step cannot point back to itself.");
            }
        }
        Map<String, Integer> colors = new HashMap<>();
        for (String key : byKey.keySet()) visit(key, byKey, colors);
        Set<String> reachable = new HashSet<>();
        collectReachable(input.nodes().getFirst().nodeKey(), byKey, reachable);
        if (reachable.size() != byKey.size()) throw new IllegalArgumentException("Every case step must be reachable from START.");
    }
    private void visit(String key, Map<String, NodeInput> graph, Map<String, Integer> colors) {
        int color = colors.getOrDefault(key, 0);
        if (color == 1) throw new IllegalArgumentException("Branching cases cannot contain cycles.");
        if (color == 2) return;
        colors.put(key, 1);
        NodeInput node = graph.get(key);
        for (ChoiceInput choice : node.choices() == null ? List.<ChoiceInput>of() : node.choices()) visit(choice.nextNodeKey(), graph, colors);
        colors.put(key, 2);
    }
    private void collectReachable(String key, Map<String, NodeInput> graph, Set<String> visited) {
        if (!visited.add(key)) return;
        NodeInput node = graph.get(key);
        for (ChoiceInput choice : node.choices() == null ? List.<ChoiceInput>of() : node.choices()) collectReachable(choice.nextNodeKey(), graph, visited);
    }
    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100.");
    }
    private <T> PageResponse<T> page(Page<?> result, List<T> content) {
        return new PageResponse<>(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
