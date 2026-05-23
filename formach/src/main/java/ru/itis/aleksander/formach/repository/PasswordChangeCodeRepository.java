package ru.itis.aleksander.formach.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.aleksander.formach.entity.PasswordChangeCode;
import ru.itis.aleksander.formach.entity.User;

import java.util.Optional;

public interface PasswordChangeCodeRepository extends JpaRepository<PasswordChangeCode, Long> {

    Optional<PasswordChangeCode> findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);
}
