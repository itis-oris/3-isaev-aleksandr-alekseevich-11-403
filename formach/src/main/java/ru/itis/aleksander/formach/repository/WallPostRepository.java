package ru.itis.aleksander.formach.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.entity.WallPost;

public interface WallPostRepository extends JpaRepository<WallPost, Long> {

    Page<WallPost> findByOwnerOrderByCreatedAtDesc(User owner, Pageable pageable);
}
