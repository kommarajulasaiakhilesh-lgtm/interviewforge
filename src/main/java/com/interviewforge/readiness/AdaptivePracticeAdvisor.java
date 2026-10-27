package com.interviewforge.readiness;

import com.interviewforge.questionbank.Difficulty;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/** Transparent, deterministic difficulty and session-size recommendations. */
final class AdaptivePracticeAdvisor {
    private static final int MAX_QUESTIONS = 5;
    private static final int MIN_ATTEMPTS_FOR_ADVANCEMENT = 5;
    private static final BigDecimal LOW_ACCURACY = BigDecimal.valueOf(60);
    private static final BigDecimal HIGH_ACCURACY = BigDecimal.valueOf(80);

    private AdaptivePracticeAdvisor() { }

    static Recommendation recommend(long answered, BigDecimal accuracy, Map<Difficulty, Long> available) {
        Difficulty preferred;
        String reason;
        if (answered == 0) {
            preferred = Difficulty.EASY;
            reason = "Start with Easy questions because there is no practice history for this topic yet.";
        } else if (accuracy.compareTo(LOW_ACCURACY) < 0) {
            preferred = Difficulty.EASY;
            reason = "Start with Easy questions to strengthen this topic after accuracy below 60%.";
        } else if (answered >= MIN_ATTEMPTS_FOR_ADVANCEMENT && accuracy.compareTo(HIGH_ACCURACY) >= 0) {
            preferred = Difficulty.HARD;
            reason = "Try Hard questions because accuracy is at least 80% across 5 or more answers.";
        } else {
            preferred = Difficulty.MEDIUM;
            reason = "Use Medium questions to build a reliable sample and consolidate this topic.";
        }

        Difficulty selected = firstAvailable(preferred, available);
        if (selected == null) {
            return new Recommendation(null, 0, 0, "No published MCQs are currently available for this topic.");
        }
        long count = available.getOrDefault(selected, 0L);
        int questions = (int) Math.min(MAX_QUESTIONS, count);
        String availabilityNote = selected == preferred ? "" :
                " " + preferred + " questions are unavailable, so this session uses " + selected + " questions.";
        return new Recommendation(selected, questions, questions * 6, reason + availabilityNote);
    }

    private static Difficulty firstAvailable(Difficulty preferred, Map<Difficulty, Long> available) {
        Map<Difficulty, Difficulty[]> fallback = new EnumMap<>(Difficulty.class);
        fallback.put(Difficulty.EASY, new Difficulty[] { Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD });
        fallback.put(Difficulty.MEDIUM, new Difficulty[] { Difficulty.MEDIUM, Difficulty.EASY, Difficulty.HARD });
        fallback.put(Difficulty.HARD, new Difficulty[] { Difficulty.HARD, Difficulty.MEDIUM, Difficulty.EASY });
        for (Difficulty difficulty : fallback.get(preferred)) {
            if (available.getOrDefault(difficulty, 0L) > 0) return difficulty;
        }
        return null;
    }

    record Recommendation(Difficulty difficulty, int questions, int estimatedMinutes, String reason) { }
}
