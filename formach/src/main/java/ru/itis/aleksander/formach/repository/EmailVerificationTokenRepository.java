package ru.itis.aleksander.formach.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.aleksander.formach.entity.EmailVerificationToken;

import java.util.Optional;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);
}