package com.interviewforge.practice;

import com.interviewforge.practice.PracticeDtos.*;
import com.interviewforge.questionbank.*;
import com.interviewforge.questionbank.QuestionBankDtos.PageResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional
public class PracticeService {
    private final QuestionRepository questions;
    private final PracticeSessionRepository sessions;
    private final PracticeSessionItemRepository items;
    private final ObjectMapper mapper;

    public PracticeService(QuestionRepository questions, PracticeSessionRepository sessions,
            PracticeSessionItemRepository items, ObjectMapper mapper) {
        this.questions = questions; this.sessions = sessions; this.items = items; this.mapper = mapper;
    }

    public CreateSessionResponse create(UUID userId, CreateSessionRequest request) {
        QuestionType type = request.type() == null ? QuestionType.MCQ : request.type();
        List<UUID> ids = questions.findRandomPublishedQuestionIds(request.topicId(),
                request.difficulty() == null ? null : request.difficulty().name(), type.name(), request.questionCount());
        if (ids.size() < request.questionCount()) throw new InsufficientQuestionsException(request.questionCount(), ids.size());
        Map<UUID, Question> byId = questions.findAllById(ids).stream().collect(Collectors.toMap(Question::getId, Function.identity()));
        PracticeSession session = sessions.save(new PracticeSession(userId, type, request.questionCount()));
        int position = 1;
        for (UUID id : ids) {
            Question q = byId.get(id);
            items.save(new PracticeSessionItem(session.getId(), id, q.getTopic().getId(), position++, q.getTitle(), q.getQuestionText(),
                    q.getDifficulty(), q.getType(), q.getOptionsJson(), q.getCorrectOptionIndex(), q.getAnswerText(), q.getExplanation()));
        }
        return new CreateSessionResponse(summary(session), items.findAllBySessionIdOrderByPosition(session.getId()).stream().map(this::prompt).toList());
    }

    public AnswerResponse submit(UUID userId, UUID sessionId, SubmitAnswerRequest request) {
        PracticeSession session = sessions.findOwnedForUpdate(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Practice session", sessionId));
        if (session.getStatus() != PracticeSessionStatus.IN_PROGRESS) throw new IllegalArgumentException("This practice session is already complete.");
        PracticeSessionItem item = items.findBySessionIdAndQuestionId(sessionId, request.questionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question in practice session", request.questionId()));
        if (item.getAnsweredAt() != null) throw new IllegalArgumentException("This question has already been answered.");
        Integer choice = request.selectedOptionIndex();
        String text = null;
        Boolean correct;
        if (item.getType() == QuestionType.MCQ) {
            List<String> options = options(item.getOptionsJson());
            if (choice == null || request.answerText() != null) throw new IllegalArgumentException("MCQ answers require selectedOptionIndex only.");
            if (choice < 0 || choice >= options.size()) throw new IllegalArgumentException("selectedOptionIndex is outside the option list.");
            correct = choice.equals(item.getCorrectOptionIndex());
        } else {
            if (choice != null || !StringUtils.hasText(request.answerText())) throw new IllegalArgumentException("TEXT answers require answerText only.");
            correct = null; text = request.answerText().trim();
        }
        item.submit(choice, text, correct);
        session.recordAnswer(Boolean.TRUE.equals(correct));
        return new AnswerResponse(sessionId, item.getQuestionId(), session.getStatus(), correct,
                item.getCorrectOptionIndex(), item.getAnswerText(), item.getExplanation(), item.getAnsweredAt(),
                session.getAnsweredCount(), session.getQuestionCount(), session.getScorePercent());
    }

    @Transactional(readOnly = true)
    public SessionDetail detail(UUID userId, UUID sessionId) {
        PracticeSession session = owned(userId, sessionId);
        return new SessionDetail(summary(session), items.findAllBySessionIdOrderByPosition(sessionId).stream().map(this::review).toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<SessionSummary> history(UUID userId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100.");
        Page<PracticeSession> result = sessions.findAllByUserIdOrderByStartedAtDesc(userId, PageRequest.of(page, size));
        return new PageResponse<>(result.getContent().stream().map(this::summary).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private PracticeSession owned(UUID userId, UUID id) {
        return sessions.findByIdAndUserId(id, userId).orElseThrow(() -> new ResourceNotFoundException("Practice session", id));
    }
    private SessionSummary summary(PracticeSession s) {
        return new SessionSummary(s.getId(), s.getStatus(), s.getStartedAt(), s.getCompletedAt(), s.getType(),
                s.getQuestionCount(), s.getAnsweredCount(), s.getCorrectCount(), s.getScorePercent());
    }
    private QuestionPrompt prompt(PracticeSessionItem i) {
        return new QuestionPrompt(i.getQuestionId(), i.getPosition(), i.getTitle(), i.getQuestionText(), i.getDifficulty(), i.getType(), options(i.getOptionsJson()));
    }
    private QuestionReview review(PracticeSessionItem i) {
        boolean answered = i.getAnsweredAt() != null;
        return new QuestionReview(i.getQuestionId(), i.getPosition(), i.getTitle(), i.getQuestionText(), i.getDifficulty(), i.getType(),
                options(i.getOptionsJson()), i.getSelectedOptionIndex(), i.getSubmittedAnswerText(), i.getCorrect(),
                answered ? i.getCorrectOptionIndex() : null, answered ? i.getAnswerText() : null,
                answered ? i.getExplanation() : null, i.getAnsweredAt());
    }
    private List<String> options(String value) {
        if (value == null) return List.of();
        try { return mapper.readValue(value, new TypeReference<>() { }); }
        catch (JacksonException e) { throw new IllegalStateException("Unable to read saved question choices.", e); }
    }
}
