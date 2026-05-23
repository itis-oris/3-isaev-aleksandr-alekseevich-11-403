package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.aleksander.formach.entity.BanAppeal;
import ru.itis.aleksander.formach.entity.ComplaintStatus;
import ru.itis.aleksander.formach.exсeption.NotFoundException;
import ru.itis.aleksander.formach.repository.BanAppealRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BanAppealService {

    private final BanAppealRepository banAppealRepository;

    @Transactional
    public BanAppeal submit(String login, String email, String message) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Логин обязателен");
        }
        if (message == null || message.trim().length() < 10) {
            throw new IllegalArgumentException("Опишите подробнее, минимум 10 символов");
        }
        BanAppeal appeal = BanAppeal.builder()
                .login(login.trim())
                .email(email == null ? null : email.trim())
                .message(message.trim())
                .status(ComplaintStatus.PENDING)
                .build();
        banAppealRepository.save(appeal);
        log.info("Поступило обращение об оспаривании бана от {}", login);
        return appeal;
    }

    @Transactional(readOnly = true)
    public Page<BanAppeal> list(String status, int page, int size) {
        if (status == null || status.isBlank()) {
            return banAppealRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        }
        return banAppealRepository.findByStatusOrderByCreatedAtDesc(
                ComplaintStatus.valueOf(status), PageRequest.of(page, size));
    }

    @Transactional
    public void setStatus(Long id, ComplaintStatus status, String response) {
        BanAppeal a = banAppealRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Обращение", id));
        a.setStatus(status);
        if (response != null && !response.isBlank()) {
            a.setResponse(response.trim());
        }
        a.setResolvedAt(LocalDateTime.now());
        banAppealRepository.save(a);
        log.info("Обращение #{} → {}", id, status);
    }

    @Transactional(readOnly = true)
    public Optional<BanAppeal> findLatestByLogin(String login) {
        if (login == null || login.isBlank()) return Optional.empty();
        return banAppealRepository.findFirstByLoginOrderByCreatedAtDesc(login.trim());
    }
}
