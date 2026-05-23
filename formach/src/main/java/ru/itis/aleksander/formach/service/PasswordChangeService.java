package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.aleksander.formach.entity.PasswordChangeCode;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.exсeption.NotFoundException;
import ru.itis.aleksander.formach.repository.PasswordChangeCodeRepository;
import ru.itis.aleksander.formach.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordChangeService {

    private static final int CODE_TTL_MINUTES = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordChangeCodeRepository codeRepository;
    private final UserRepository userRepository;
    private final EmailSenderService emailSenderService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestCode(User user) {
        String code = generateCode();
        PasswordChangeCode entity = PasswordChangeCode.builder()
                .user(user)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES))
                .used(false)
                .build();
        codeRepository.save(entity);

        emailSenderService.sendPasswordChangeCode(user.getEmail(), code);
        log.info("Код смены пароля отправлен пользователю {}", user.getLogin());
    }

    @Transactional
    public void confirm(User user, String code, String newPassword) {
        if (newPassword == null || !newPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,50}$")) {
            throw new IllegalArgumentException(
                    "Пароль должен быть 8-50 символов, содержать цифру, строчную и заглавную букву");
        }

        PasswordChangeCode entity = codeRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new NotFoundException("Код подтверждения",
                        "сначала запросите код"));

        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Срок действия кода истёк, запросите новый");
        }
        if (!entity.getCode().equals(code != null ? code.trim() : null)) {
            throw new IllegalArgumentException("Неверный код");
        }

        entity.setUsed(true);
        codeRepository.save(entity);

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Пароль изменён для пользователя {}", user.getLogin());
    }

    private String generateCode() {
        return String.valueOf(RANDOM.nextInt(100000, 1000000));
    }
}
