package com.interviewforge.questionbank;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.interviewforge.questionbank.QuestionBankDtos.CatalogInput;
import com.interviewforge.questionbank.QuestionBankDtos.PageResponse;
import com.interviewforge.questionbank.QuestionBankDtos.QuestionDetail;
import com.interviewforge.questionbank.QuestionBankDtos.QuestionInput;
import com.interviewforge.questionbank.QuestionBankDtos.QuestionSummary;
import com.interviewforge.questionbank.QuestionBankDtos.TagResponse;
import com.interviewforge.questionbank.QuestionBankDtos.TopicResponse;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class QuestionBankService {
    private final TopicRepository topics;
    private final TagRepository tags;
    private final QuestionRepository questions;
    private final ObjectMapper objectMapper;

    public QuestionBankService(TopicRepository topics, TagRepository tags, QuestionRepository questions, ObjectMapper objectMapper) {
        this.topics = topics; this.tags = tags; this.questions = questions; this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> listTopics() { return topics.findAllByActiveTrueOrderByNameAsc().stream().map(this::topicResponse).toList(); }
    @Transactional(readOnly = true)
    public List<TagResponse> listTags() { return tags.findAllByActiveTrueOrderByNameAsc().stream().map(this::tagResponse).toList(); }
    @Transactional(readOnly = true)
    public List<TopicResponse> adminListTopics() { return topics.findAllByOrderByNameAsc().stream().map(this::topicResponse).toList(); }
    @Transactional(readOnly = true)
    public List<TagResponse> adminListTags() { return tags.findAllByOrderByNameAsc().stream().map(this::tagResponse).toList(); }

    public TopicResponse createTopic(CatalogInput input) {
        return topicResponse(topics.save(new Topic(input.name().trim(), slug(input.slug(), input.name()), input.description())));
    }
    public TopicResponse updateTopic(UUID id, CatalogInput input) {
        Topic topic = topics.findById(id).orElseThrow(() -> new ResourceNotFoundException("Topic", id));
        topic.update(input.name().trim(), slug(input.slug(), input.name()), input.description(), input.active() == null || input.active());
        return topicResponse(topic);
    }
    public TagResponse createTag(CatalogInput input) {
        return tagResponse(tags.save(new Tag(input.name().trim(), slug(input.slug(), input.name()))));
    }
    public TagResponse updateTag(UUID id, CatalogInput input) {
        Tag tag = tags.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        tag.update(input.name().trim(), slug(input.slug(), input.name()), input.active() == null || input.active());
        return tagResponse(tag);
    }

    @Transactional(readOnly = true)
    public PageResponse<QuestionSummary> browse(UUID topicId, UUID tagId, Difficulty difficulty, String search, int page, int size) {
        Specification<Question> spec = (root, query, cb) -> cb.and(
                cb.isTrue(root.get("published")), cb.isFalse(root.get("archived")), cb.isTrue(root.join("topic").get("active")));
        if (topicId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("topic").get("id"), topicId));
        if (difficulty != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("difficulty"), difficulty));
        if (StringUtils.hasText(search)) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(cb.like(cb.lower(root.get("title")), pattern), cb.like(cb.lower(root.get("questionText")), pattern)));
        }
        if (tagId != null) spec = spec.and((root, query, cb) -> {
            query.distinct(true);
            var join = root.join("tags");
            return cb.and(cb.equal(join.get("id"), tagId), cb.isTrue(join.get("active")));
        });
        Page<Question> result = questions.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResponse<>(result.getContent().stream().map(this::summary).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public QuestionSummary getPublishedQuestion(UUID id) {
        Question question = questions.findById(id).filter(q -> q.isPublished() && !q.isArchived() && q.getTopic().isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));
        return summary(question);
    }

    public QuestionDetail createQuestion(QuestionInput input) {
        Topic topic = findTopic(input.topicId());
        Set<Tag> selectedTags = resolveTags(input.tagIds());
        validateAnswer(input);
        Question question = new Question(topic, input.title().trim(), input.questionText().trim(), input.difficulty(), input.type(),
                optionsJson(input.options()), input.correctOptionIndex(), input.answerText(), input.explanation(),
                optionsJson(input.optionExplanations()), input.theoryNotes(), input.workplaceExample(), input.misconceptionLabel(),
                input.scenarioContext(), input.interviewStage(), input.sourceType(), input.sourceLabel(), input.sourceUrl(),
                input.sourceVerifiedAt(), input.evaluationCriteria(), input.followUpPrompt(), selectedTags,
                Boolean.TRUE.equals(input.published()));
        return detail(questions.save(question));
    }
    public QuestionDetail updateQuestion(UUID id, QuestionInput input) {
        Question question = questions.findById(id).orElseThrow(() -> new ResourceNotFoundException("Question", id));
        validateAnswer(input);
        question.update(findTopic(input.topicId()), input.title().trim(), input.questionText().trim(), input.difficulty(), input.type(),
                optionsJson(input.options()), input.correctOptionIndex(), input.answerText(), input.explanation(),
                optionsJson(input.optionExplanations()), input.theoryNotes(), input.workplaceExample(), input.misconceptionLabel(),
                input.scenarioContext(), input.interviewStage(), input.sourceType(), input.sourceLabel(), input.sourceUrl(),
                input.sourceVerifiedAt(), input.evaluationCriteria(), input.followUpPrompt(), resolveTags(input.tagIds()),
                Boolean.TRUE.equals(input.published()));
        return detail(question);
    }
    public void archiveQuestion(UUID id) {
        Question question = questions.findById(id).orElseThrow(() -> new ResourceNotFoundException("Question", id));
        question.archive();
    }

    @Transactional(readOnly = true)
    public PageResponse<QuestionDetail> adminBrowse(int page, int size) {
        Page<Question> result = questions.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResponse<>(result.getContent().stream().map(this::detail).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public QuestionDetail adminGet(UUID id) {
        return detail(questions.findById(id).orElseThrow(() -> new ResourceNotFoundException("Question", id)));
    }

    private Topic findTopic(UUID id) { return topics.findById(id).orElseThrow(() -> new ResourceNotFoundException("Topic", id)); }
    private Set<Tag> resolveTags(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Set.of();
        List<Tag> found = tags.findAllById(ids);
        if (found.size() != ids.stream().distinct().count()) throw new ResourceNotFoundException("One or more tags", ids);
        return found.stream().collect(Collectors.toSet());
    }
    private void validateAnswer(QuestionInput input) {
        if (input.sourceType() != null && !input.sourceType().isBlank()
                && !Set.of("ORIGINAL", "OFFICIAL", "COMMUNITY_REPORTED", "EDITORIAL", "UNKNOWN").contains(input.sourceType())) {
            throw new IllegalArgumentException("sourceType must be ORIGINAL, OFFICIAL, COMMUNITY_REPORTED, EDITORIAL, or UNKNOWN.");
        }
        if (input.sourceUrl() != null && !input.sourceUrl().isBlank()
                && !(input.sourceUrl().startsWith("https://") || input.sourceUrl().startsWith("http://"))) {
            throw new IllegalArgumentException("sourceUrl must use http or https.");
        }
        if (input.type() == QuestionType.MCQ) {
            if (input.options() == null || input.options().size() < 2 || input.correctOptionIndex() == null
                    || input.correctOptionIndex() < 0 || input.correctOptionIndex() >= input.options().size()) {
                throw new IllegalArgumentException("MCQ questions require at least two options and a valid correctOptionIndex.");
            }
            if (input.optionExplanations() != null && input.optionExplanations().size() != input.options().size()) {
                throw new IllegalArgumentException("optionExplanations must contain one explanation for each option.");
            }
        } else if ((input.options() != null && !input.options().isEmpty()) || input.correctOptionIndex() != null) {
            throw new IllegalArgumentException("TEXT questions cannot include multiple-choice options or a correctOptionIndex.");
        } else if (input.optionExplanations() != null && !input.optionExplanations().isEmpty()) {
            throw new IllegalArgumentException("TEXT questions cannot include optionExplanations.");
        }
    }
    private String optionsJson(List<String> options) {
        try { return options == null || options.isEmpty() ? null : objectMapper.writeValueAsString(options); }
        catch (JacksonException exception) { throw new IllegalStateException("Unable to encode question options.", exception); }
    }
    private List<String> options(Question question) {
        try { return question.getOptionsJson() == null ? List.of() : objectMapper.readValue(question.getOptionsJson(), new TypeReference<>() { }); }
        catch (JacksonException exception) { throw new IllegalStateException("Unable to read question options.", exception); }
    }
    private String slug(String provided, String name) {
        String source = StringUtils.hasText(provided) ? provided : name;
        return source.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
    private TopicResponse topicResponse(Topic topic) { return new TopicResponse(topic.getId(), topic.getName(), topic.getSlug(), topic.getDescription(), topic.isActive()); }
    private TagResponse tagResponse(Tag tag) { return new TagResponse(tag.getId(), tag.getName(), tag.getSlug(), tag.isActive()); }
    private List<TagResponse> tagResponses(Question question) { return question.getTags().stream().map(this::tagResponse).toList(); }
    private List<String> explanations(Question q) {
        try { return q.getOptionExplanationsJson() == null ? List.of() : objectMapper.readValue(q.getOptionExplanationsJson(), new TypeReference<>() { }); }
        catch (JacksonException exception) { throw new IllegalStateException("Unable to read answer explanations.", exception); }
    }
    private QuestionSummary summary(Question q) { return new QuestionSummary(q.getId(), q.getTitle(), q.getQuestionText(), q.getDifficulty(), q.getType(), options(q), topicResponse(q.getTopic()), tagResponses(q), q.getCreatedAt(), q.getScenarioContext(), q.getInterviewStage(), q.getSourceType(), q.getSourceLabel(), q.getSourceUrl(), q.getSourceVerifiedAt()); }
    private QuestionDetail detail(Question q) { return new QuestionDetail(q.getId(), q.getTitle(), q.getQuestionText(), q.getDifficulty(), q.getType(), options(q), topicResponse(q.getTopic()), tagResponses(q), q.getCreatedAt(), q.getUpdatedAt(), q.isPublished(), q.getCorrectOptionIndex(), q.getAnswerText(), q.getExplanation(), explanations(q), q.getTheoryNotes(), q.getWorkplaceExample(), q.getMisconceptionLabel(), q.getScenarioContext(), q.getInterviewStage(), q.getSourceType(), q.getSourceLabel(), q.getSourceUrl(), q.getSourceVerifiedAt(), q.getEvaluationCriteria(), q.getFollowUpPrompt()); }
}
