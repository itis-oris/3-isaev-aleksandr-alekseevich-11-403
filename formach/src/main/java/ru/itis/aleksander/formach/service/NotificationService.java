package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.aleksander.formach.entity.Notification;
import ru.itis.aleksander.formach.entity.NotificationType;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void notify(User recipient, User actor, NotificationType type,
                       String message, String link) {
        if (recipient == null) return;
        if (actor != null && actor.getId().equals(recipient.getId())) return;
        Notification n = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .message(message)
                .link(link)
                .isRead(false)
                .build();
        notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public long countUnread(User user) {
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    @Transactional(readOnly = true)
    public Page<Notification> list(User user, int page, int size) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user,
                PageRequest.of(page, size));
    }

    @Transactional
    public void markAllRead(User user) {
        notificationRepository.markAllRead(user);
    }
}
