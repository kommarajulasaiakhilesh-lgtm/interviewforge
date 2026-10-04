package com.interviewforge.questionbank;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
    List<Topic> findAllByActiveTrueOrderByNameAsc();
    List<Topic> findAllByOrderByNameAsc();
}
