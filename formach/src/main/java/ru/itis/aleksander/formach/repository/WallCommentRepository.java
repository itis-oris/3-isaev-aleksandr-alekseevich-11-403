package ru.itis.aleksander.formach.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.aleksander.formach.entity.WallComment;

public interface WallCommentRepository extends JpaRepository<WallComment, Long> {
}
