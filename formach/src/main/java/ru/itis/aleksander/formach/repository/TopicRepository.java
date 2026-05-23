package ru.itis.aleksander.formach.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.aleksander.formach.entity.Topic;
import ru.itis.aleksander.formach.entity.TopicType;
import ru.itis.aleksander.formach.entity.User;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long>, TopicRepositoryCustom {

    long countByAuthor(User author);

    @Query("SELECT t FROM Topic t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Topic> searchByTitle(@Param("q") String q, Pageable pageable);

    Page<Topic> findByAuthor(User author, Pageable pageable);

    @Query("SELECT t FROM Topic t WHERE t.type = :type " +
           "AND t.bestAnswer IS NOT NULL " +
           "AND LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Topic> findSolved(@Param("q") String q,
                           @Param("type") TopicType type,
                           Pageable pageable);

    @Query("SELECT t FROM Topic t WHERE t.type = :type " +
           "AND t.bestAnswer IS NULL AND t.isClosed = false " +
           "AND LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Topic> findUnsolved(@Param("q") String q,
                             @Param("type") TopicType type,
                             Pageable pageable);

    @Query("SELECT t FROM Topic t WHERE t.type = :type " +
           "AND LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Topic> findByType(@Param("q") String q,
                           @Param("type") TopicType type,
                           Pageable pageable);

    @Query("SELECT t FROM Topic t JOIN t.tags tg " +
           "WHERE tg.id IN :tagIds AND LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "GROUP BY t.id " +
           "HAVING COUNT(DISTINCT tg.id) = :tagCount")
    Page<Topic> findByAllTags(@Param("q") String q,
                              @Param("tagIds") List<Long> tagIds,
                              @Param("tagCount") long tagCount,
                              Pageable pageable);

    @Query("SELECT t FROM Topic t WHERE t.author IN :authors ORDER BY t.createdAt DESC")
    List<Topic> findByAuthorInRecent(@Param("authors") List<User> authors, Pageable pageable);

    @Query("""
            SELECT t FROM Topic t
            LEFT JOIN t.posts p
            WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%'))
            GROUP BY t.id
            ORDER BY t.pinnedByAdmin DESC, COUNT(p) DESC, t.createdAt DESC
            """)
    Page<Topic> searchOrderByPosts(@Param("q") String q, Pageable pageable);

    @Query("""
            SELECT t FROM Topic t
            WHERE EXISTS (
                SELECT 1 FROM Post p
                WHERE p.topic = t AND p.author = :user
            )
            ORDER BY t.createdAt DESC
            """)
    Page<Topic> findTopicsWhereUserPosted(@Param("user") User user, Pageable pageable);
}
