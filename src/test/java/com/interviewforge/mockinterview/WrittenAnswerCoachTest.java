package com.interviewforge.mockinterview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WrittenAnswerCoachTest {
    private final WrittenAnswerCoach coach = new WrittenAnswerCoach();

    @Test
    void highlightsActionOutcomeAndSpecificEvidenceSignals() {
        String answer = "I implemented a queue change for the billing service. For example, I measured 25% lower processing latency after the release. "
                + "I reviewed the rollout with support and documented the recovery steps. This improved the handoff for the next deployment. "
                + "The team monitored the service for two weeks and confirmed the change held during peak traffic.";

        var feedback = coach.coach(answer, null);

        assertTrue(feedback.strengths().stream().anyMatch(s -> s.contains("first-person action")));
        assertTrue(feedback.strengths().stream().anyMatch(s -> s.contains("outcome")));
        assertTrue(feedback.strengths().stream().anyMatch(s -> s.contains("concrete detail")));
        assertEquals(0, feedback.rubricEvidence().size());
    }

    @Test
    void suggestsMissingPersonalActionsOutcomeAndSpecificity() {
        var feedback = coach.coach("It was a difficult project and we had several issues.", null);

        assertTrue(feedback.improvements().stream().anyMatch(s -> s.contains("personally did")));
        assertTrue(feedback.improvements().stream().anyMatch(s -> s.contains("what changed")));
        assertTrue(feedback.improvements().stream().anyMatch(s -> s.contains("verifiable detail")));
    }

    @Test
    void rubricOverlapIsPresentedAsPossibleEvidenceRatherThanAmetCriterion() {
        var feedback = coach.coach("I communicated project risks to the team and improved response time by 20%.",
                "- Communicate project risks\n- Improve response time");

        assertEquals(2, feedback.rubricEvidence().size());
        assertFalse(feedback.rubricEvidence().getFirst().possibleOverlapTerms().isEmpty());
        assertTrue(feedback.rubricEvidence().getFirst().coachingNote().contains("Possible text overlap"));
        assertTrue(feedback.methodDisclosure().contains("not semantic grading"));
    }
}
