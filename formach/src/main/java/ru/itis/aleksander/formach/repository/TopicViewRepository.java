package ru.itis.aleksander.formach.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.aleksander.formach.entity.TopicView;
import ru.itis.aleksander.formach.entity.Topic;
import ru.itis.aleksander.formach.entity.User;

import java.util.Optional;

public interface TopicViewRepository extends JpaRepository<TopicView, Long> {

    Optional<TopicView> findByTopicAndUser(Topic topic, User user);

    Page<TopicView> findByUserOrderByViewedAtDesc(User user, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM TopicView tv WHERE tv.topic = :topic")
    void deleteByTopic(@Param("topic") Topic topic);
}