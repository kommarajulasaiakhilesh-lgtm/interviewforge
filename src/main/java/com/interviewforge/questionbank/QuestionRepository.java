package com.interviewforge.questionbank;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, UUID>, JpaSpecificationExecutor<Question> {
    @Query(value = "SELECT q.id FROM questions q JOIN topics t ON t.id=q.topic_id WHERE q.published=TRUE AND q.archived=FALSE AND t.active=TRUE AND (:topicId IS NULL OR q.topic_id=:topicId) AND (:difficulty IS NULL OR q.difficulty=:difficulty) AND q.type=:type ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    java.util.List<UUID> findRandomPublishedQuestionIds(@Param("topicId") UUID topicId, @Param("difficulty") String difficulty,
            @Param("type") String type, @Param("limit") int limit);

    @Query(value = "SELECT q.* FROM questions q JOIN topics t ON t.id=q.topic_id "
            + "WHERE q.topic_id=:topicId AND q.type='TEXT' AND q.published=TRUE AND q.archived=FALSE AND t.active=TRUE "
            + "ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    java.util.List<Question> findRandomPublishedTextByTopic(@Param("topicId") UUID topicId, @Param("limit") int limit);
}
