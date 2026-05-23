package ru.itis.aleksander.formach.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.aleksander.formach.entity.Post;
import ru.itis.aleksander.formach.entity.Topic;
import ru.itis.aleksander.formach.entity.User;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByTopicAndParentIsNullOrderByIsPinnedDescCreatedAtAsc(Topic topic);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.author = :user AND p.parent IS NULL")
    long countTopLevelByAuthor(@Param("user") User user);

    Page<Post> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);
}
