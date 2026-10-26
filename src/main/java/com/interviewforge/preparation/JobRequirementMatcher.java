package com.interviewforge.preparation;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

final class JobRequirementMatcher {
    private static final Pattern BULLET_PREFIX = Pattern.compile("^\\s*(?:[-*•▪]+|\\d+[.)])\\s*");
    private static final Pattern SENTENCE_BREAK = Pattern.compile("[;.!?]+\\s+");
    private static final Set<String> STOP_WORDS = Set.of("and", "or", "the", "a", "an", "to", "of", "in", "with", "for", "on", "is", "are", "be", "have", "has", "will", "you", "we", "our", "their", "this", "that", "from", "by", "as", "at", "about", "ability", "experience", "knowledge", "strong", "proven", "working", "familiarity");
    private static final Map<String, List<String>> ALIASES = aliases();

    private JobRequirementMatcher() { }

    static List<String> extract(String jobDescription) {
        Map<String, String> unique = new LinkedHashMap<>();
        for (String line : jobDescription.split("\\R")) {
            String cleaned = BULLET_PREFIX.matcher(line).replaceFirst("").trim();
            for (String fragment : SENTENCE_BREAK.split(cleaned)) {
                String requirement = fragment.trim().replaceAll("\\s+", " ");
                if (requirement.length() < 24 || significantWordCount(requirement) < 3) continue;
                unique.putIfAbsent(normalize(requirement), requirement);
                if (unique.size() == 100) return List.copyOf(unique.values());
            }
        }
        return List.copyOf(unique.values());
    }

    static boolean matches(String requirement, String skillSlug, String skillName) {
        String normalizedRequirement = " " + normalize(requirement) + " ";
        List<String> candidates = new ArrayList<>();
        candidates.add(normalize(skillName));
        candidates.addAll(ALIASES.getOrDefault(skillSlug, List.of()));
        for (String candidate : candidates) {
            String normalizedCandidate = normalize(candidate);
            if (normalizedCandidate.length() >= 3 && normalizedRequirement.contains(" " + normalizedCandidate + " ")) return true;
        }
        return false;
    }

    private static int significantWordCount(String text) {
        return (int) List.of(normalize(text).split(" ")).stream().filter(word -> word.length() > 1 && !STOP_WORDS.contains(word)).count();
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9+#.]+", " ").trim().replaceAll("\\s+", " ");
    }

    private static Map<String, List<String>> aliases() {
        Map<String, List<String>> aliases = new LinkedHashMap<>();
        aliases.put("problem-solving-algorithms", List.of("data structures", "algorithms", "algorithmic complexity", "problem solving", "dsa", "big o"));
        aliases.put("object-oriented-design", List.of("object oriented", "oop", "design patterns", "software design"));
        aliases.put("database-fundamentals", List.of("database", "databases", "sql", "relational data", "postgres", "postgresql", "transactions", "indexes"));
        aliases.put("concurrency-operating-systems", List.of("concurrency", "operating systems", "threads", "threading", "processes", "parallel processing", "synchronization"));
        aliases.put("networking-fundamentals", List.of("networking", "computer networks", "http", "dns", "tcp", "network protocols"));
        aliases.put("system-design", List.of("system design", "distributed systems", "scalability", "scalable services", "reliability", "caching"));
        aliases.put("communication-collaboration", List.of("communication", "collaboration", "teamwork", "stakeholder management", "conflict resolution", "behavioral interview"));
        return Map.copyOf(aliases);
    }
}
