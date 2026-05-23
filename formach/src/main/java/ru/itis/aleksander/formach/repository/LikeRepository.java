package ru.itis.aleksander.formach.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.aleksander.formach.entity.Like;
import ru.itis.aleksander.formach.entity.Post;
import ru.itis.aleksander.formach.entity.User;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByPostAndUser(Post post, User user);

    void deleteByPostAndUser(Post post, User user);

    long countByPost(Post post);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.author = :user")
    long countReceivedByUser(@Param("user") User user);
}