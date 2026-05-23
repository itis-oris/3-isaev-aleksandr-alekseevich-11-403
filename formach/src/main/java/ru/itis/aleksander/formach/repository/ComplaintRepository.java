package ru.itis.aleksander.formach.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.aleksander.formach.entity.Complaint;
import ru.itis.aleksander.formach.entity.ComplaintStatus;
import ru.itis.aleksander.formach.entity.Topic;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Page<Complaint> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status, Pageable pageable);

    long countByStatus(ComplaintStatus status);

    @Modifying
    @Query("DELETE FROM Complaint c WHERE c.topic = :topic")
    void deleteByTopic(@Param("topic") Topic topic);
}