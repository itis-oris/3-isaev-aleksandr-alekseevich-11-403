package ru.itis.aleksander.formach.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.aleksander.formach.entity.BanAppeal;
import ru.itis.aleksander.formach.entity.ComplaintStatus;

import java.util.Optional;

public interface BanAppealRepository extends JpaRepository<BanAppeal, Long> {

    Page<BanAppeal> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<BanAppeal> findByStatusOrderByCreatedAtDesc(ComplaintStatus status, Pageable pageable);

    Optional<BanAppeal> findFirstByLoginOrderByCreatedAtDesc(String login);
}
