package com.interviewforge.progress;

import com.interviewforge.practice.PracticeSessionItem;
import com.interviewforge.progress.ProgressProjection.OverviewRow;
import com.interviewforge.progress.ProgressProjection.TopicRow;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ProgressRepository extends Repository<PracticeSessionItem, UUID> {
    @Query(value = "SELECT COUNT(DISTINCT s.id) FILTER (WHERE s.status='COMPLETED') AS \"completedSessions\", "
            + "COUNT(DISTINCT s.id) FILTER (WHERE s.status='IN_PROGRESS') AS \"inProgressSessions\", "
            + "COUNT(i.id) FILTER (WHERE i.type='MCQ' AND i.answered_at IS NOT NULL) AS \"mcqQuestionsAnswered\", "
            + "COUNT(i.id) FILTER (WHERE i.type='MCQ' AND i.answered_at IS NOT NULL AND i.is_correct=TRUE) AS \"mcqCorrectAnswers\" "
            + "FROM practice_sessions s LEFT JOIN practice_session_items i ON i.session_id=s.id WHERE s.user_id=:userId", nativeQuery = true)
    OverviewRow overview(@Param("userId") UUID userId);

    @Query(value = "SELECT i.topic_id AS \"topicId\", t.name AS \"topicName\", COUNT(*) AS \"questionsAnswered\", "
            + "COUNT(*) FILTER (WHERE i.is_correct=TRUE) AS \"correctAnswers\", "
            + "(EXTRACT(EPOCH FROM MAX(i.answered_at))*1000)::BIGINT AS \"lastAttemptEpoch\" "
            + "FROM practice_session_items i JOIN practice_sessions s ON s.id=i.session_id "
            + "JOIN topics t ON t.id=i.topic_id WHERE s.user_id=:userId AND i.type='MCQ' AND i.answered_at IS NOT NULL "
            + "GROUP BY i.topic_id, t.name ORDER BY COUNT(*) DESC, t.name ASC", nativeQuery = true)
    List<TopicRow> topics(@Param("userId") UUID userId);
}
