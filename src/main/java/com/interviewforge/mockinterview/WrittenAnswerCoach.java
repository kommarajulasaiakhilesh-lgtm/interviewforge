package com.interviewforge.mockinterview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import static com.interviewforge.mockinterview.MockInterviewDtos.*;

/**
 * Private, deterministic coaching from visible answer signals. This deliberately
 * avoids claiming semantic grading or sending learner answers to an AI provider.
 */
final class WrittenAnswerCoach {
    private static final int MIN_SUGGESTED_WORDS = 80;
    private static final int MAX_SUGGESTED_WORDS = 300;
    private static final Pattern TOKEN = Pattern.compile("[a-z0-9%]+");
    private static final Pattern ACTION = Pattern.compile(
            "\\b(i|my)\\b.{0,80}\\b(built|changed|created|decided|designed|developed|drove|implemented|improved|investigated|led|measured|migrated|negotiated|organized|prioritized|proposed|reduced|resolved|tested|wrote)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern RESULT = Pattern.compile(
            "\\b(result|impact|outcome|improv|increas|decreas|reduc|sav(ed|ing)|deliver(ed|y)|achiev(ed|ement)|complet(ed|ion)|learn(ed|ing)|percent|customer|latency|revenue|cost)\\w*\\b|\\b\\d+(?:\\.\\d+)?%?\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Set<String> STOP_WORDS = Set.of(
            "about", "after", "also", "and", "are", "because", "been", "being", "between", "from",
            "have", "into", "more", "over", "that", "their", "then", "there", "these", "they",
            "this", "through", "under", "using", "were", "what", "when", "where", "which", "with",
            "your", "you", "the", "for", "was", "not", "but", "can", "could", "should", "would");

    AnswerFeedback coach(String answer, String rubric) {
        String text = answer == null ? "" : answer.trim();
        int wordCount = text.isEmpty() ? 0 : text.split("\\s+").length;
        boolean actionEvidence = ACTION.matcher(text).find();
        boolean resultEvidence = RESULT.matcher(text).find();
        boolean detailEvidence = Pattern.compile("\\d|for example|for instance|such as", Pattern.CASE_INSENSITIVE)
                .matcher(text).find();

        List<String> strengths = new ArrayList<>();
        if (actionEvidence) strengths.add("The response names a first-person action, which helps clarify your contribution.");
        if (resultEvidence) strengths.add("The response includes outcome or impact language.");
        if (detailEvidence) strengths.add("The response contains a concrete detail such as a number or example cue.");
        if (wordCount >= MIN_SUGGESTED_WORDS && wordCount <= MAX_SUGGESTED_WORDS)
            strengths.add("The response is within the suggested 80–300 word range.");
        if (strengths.isEmpty()) strengths.add("You have a starting draft; use the prompts below to add specific evidence.");

        List<String> improvements = new ArrayList<>();
        if (wordCount < MIN_SUGGESTED_WORDS)
            improvements.add("Add brief context and enough detail for the interviewer to understand the situation and your responsibility.");
        if (wordCount > MAX_SUGGESTED_WORDS)
            improvements.add("Tighten the background and keep the focus on the decision, your actions, and the outcome.");
        if (!actionEvidence)
            improvements.add("State what you personally did and why; use concrete actions rather than only describing what the team did.");
        if (!resultEvidence)
            improvements.add("Explain what changed because of your actions, using a measurable result or a clear lesson where possible.");
        if (!detailEvidence)
            improvements.add("Add one verifiable detail, such as scale, time saved, a constraint, or a specific example.");
        if (improvements.isEmpty())
            improvements.add("Check that the evidence is relevant to the question and that you can explain your choices in a follow-up.");

        List<RubricEvidence> rubricEvidence = rubricLines(rubric).stream().map(line -> rubricEvidence(line, text)).toList();
        return new AnswerFeedback(wordCount, strengths, improvements, rubricEvidence,
                "Rule-based writing cues and literal rubric-term overlap only. This is not semantic grading, a hiring prediction, or proof that a criterion was met.");
    }

    private List<String> rubricLines(String rubric) {
        if (rubric == null || rubric.isBlank()) return List.of();
        return Arrays.stream(rubric.split("(?:\\R|;|(?m)^\\s*[-•*]\\s*)"))
                .map(String::trim).filter(line -> !line.isEmpty()).limit(12).toList();
    }

    private RubricEvidence rubricEvidence(String criterion, String answer) {
        List<String> criterionTerms = terms(criterion);
        List<String> answerTerms = terms(answer);
        List<String> overlap = criterionTerms.stream().filter(answerTerms::contains).distinct().limit(8).toList();
        String note = overlap.isEmpty()
                ? "No matching terms found. Review manually whether your answer addresses this criterion in your own words."
                : "Possible text overlap: " + String.join(", ", overlap) + ". Check that your example actually demonstrates the criterion.";
        return new RubricEvidence(criterion, overlap, note);
    }

    private List<String> terms(String text) {
        var matcher = TOKEN.matcher(text.toLowerCase(Locale.ROOT));
        List<String> terms = new ArrayList<>();
        while (matcher.find()) {
            String token = matcher.group();
            if (token.length() >= 4 && !STOP_WORDS.contains(token)) terms.add(token);
        }
        return terms;
    }

}
