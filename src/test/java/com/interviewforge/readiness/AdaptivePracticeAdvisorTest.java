package com.interviewforge.readiness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.interviewforge.questionbank.Difficulty;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AdaptivePracticeAdvisorTest {
    @Test
    void startsNewTopicsAtEasyWhenAvailable() {
        var result = AdaptivePracticeAdvisor.recommend(0, BigDecimal.ZERO, Map.of(Difficulty.EASY, 8L));
        assertEquals(Difficulty.EASY, result.difficulty());
        assertEquals(5, result.questions());
        assertEquals(30, result.estimatedMinutes());
    }

    @Test
    void fallsBackToAvailableDifficultyAndCapsSessionSize() {
        var result = AdaptivePracticeAdvisor.recommend(2, BigDecimal.valueOf(40), Map.of(Difficulty.MEDIUM, 3L));
        assertEquals(Difficulty.MEDIUM, result.difficulty());
        assertEquals(3, result.questions());
        assertEquals(18, result.estimatedMinutes());
        assertTrue(result.reason().contains("Easy questions are unavailable"));
    }

    @Test
    void advancesReliableHighAccuracyTopicsToHard() {
        var result = AdaptivePracticeAdvisor.recommend(7, BigDecimal.valueOf(86), Map.of(Difficulty.HARD, 4L));
        assertEquals(Difficulty.HARD, result.difficulty());
        assertEquals(4, result.questions());
        assertEquals(24, result.estimatedMinutes());
    }

    @Test
    void returnsNoPracticeWhenTopicHasNoPublishedMcqs() {
        var result = AdaptivePracticeAdvisor.recommend(0, BigDecimal.ZERO, Map.of());
        assertNull(result.difficulty());
        assertEquals(0, result.questions());
        assertEquals(0, result.estimatedMinutes());
        assertTrue(result.reason().contains("No published MCQs"));
    }
}
