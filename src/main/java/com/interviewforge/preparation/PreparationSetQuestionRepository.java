package com.interviewforge.preparation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PreparationSetQuestionRepository extends JpaRepository<PreparationSetQuestion,UUID> {
    List<PreparationSetQuestion> findAllBySetIdOrderByPosition(UUID setId);
    void deleteAllBySetId(UUID setId);
}
