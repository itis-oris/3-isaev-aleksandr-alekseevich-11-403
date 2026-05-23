package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.itis.aleksander.formach.entity.EmailVerificationToken;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.exсeption.NotFoundException;
import ru.itis.aleksander.formach.exсeption.TokenExpiredException;
import ru.itis.aleksander.formach.repository.EmailVerificationTokenRepository;
import ru.itis.aleksander.formach.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailSenderService emailSenderService;

    public void sendVerification(User user) {
        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        tokenRepository.save(verificationToken);
        emailSenderService.sendVerificationEmail(user.getEmail(), token);

        log.info("Токен подтверждения создан для {}", user.getEmail());
    }

    public void verify(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Токен", token));

        if (verificationToken.getUsed()) {
            throw new RuntimeException("Токен уже использован");
        }

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException();
        }

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email подтверждён для {}", user.getEmail());
    }
}