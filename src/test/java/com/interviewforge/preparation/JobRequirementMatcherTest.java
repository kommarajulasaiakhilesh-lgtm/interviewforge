package com.interviewforge.preparation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class JobRequirementMatcherTest {
    @Test
    void extractsUniqueRequirementSentencesFromBullets() {
        List<String> requirements = JobRequirementMatcher.extract("""
                Responsibilities
                - Experience with SQL query optimization and relational data modeling.
                - Experience with SQL query optimization and relational data modeling.
                2. Design scalable distributed systems for customer-facing services
                """);

        assertEquals(2, requirements.size());
        assertTrue(requirements.get(0).startsWith("Experience with SQL"));
        assertTrue(requirements.get(1).startsWith("Design scalable"));
    }

    @Test
    void matchesKnownSkillPhrasesAndKeepsUnmatchedRequirementsExplicit() {
        assertTrue(JobRequirementMatcher.matches("Optimize SQL transactions and indexes", "database-fundamentals", "Database Fundamentals"));
        assertTrue(JobRequirementMatcher.matches("Design scalable services", "system-design", "System Design"));
        assertFalse(JobRequirementMatcher.matches("Maintain accurate project documentation", "database-fundamentals", "Database Fundamentals"));
    }
}
