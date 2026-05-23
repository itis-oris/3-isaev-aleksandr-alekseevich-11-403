package ru.itis.aleksander.formach.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.aleksander.formach.entity.Bookmark;
import ru.itis.aleksander.formach.entity.Topic;
import ru.itis.aleksander.formach.entity.User;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByUserAndTopic(User user, Topic topic);

    boolean existsByUserAndTopic(User user, Topic topic);

    List<Bookmark> findByUserOrderByCreatedAtDesc(User user);

    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.topic = :topic")
    void deleteByTopic(@Param("topic") Topic topic);
}
