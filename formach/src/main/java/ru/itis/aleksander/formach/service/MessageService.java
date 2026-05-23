package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.aleksander.formach.entity.Attachment;
import ru.itis.aleksander.formach.entity.Message;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.exсeption.NotFoundException;
import ru.itis.aleksander.formach.repository.AttachmentRepository;
import ru.itis.aleksander.formach.repository.MessageRepository;
import ru.itis.aleksander.formach.repository.UserRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    public List<User> getInterlocutors(User user) {
        LinkedHashSet<User> result = new LinkedHashSet<>();
        result.addAll(messageRepository.findReceiversBySender(user));
        result.addAll(messageRepository.findSendersByReceiver(user));
        return new ArrayList<>(result);
    }

    public List<Message> getConversation(User currentUser, Long withUserId) {
        User other = userRepository.findById(withUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь", withUserId));
        return messageRepository.findConversation(currentUser, other);
    }

    @Transactional
    public void send(User sender, Long receiverId, String content, List<MultipartFile> files) {
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new NotFoundException("Пользователь", receiverId));

        boolean hasContent = content != null && !content.isBlank();
        boolean hasFiles = files != null && files.stream().anyMatch(f -> f != null && !f.isEmpty());
        if (!hasContent && !hasFiles) {
            throw new IllegalArgumentException("Сообщение пустое — добавьте текст или хотя бы один файл");
        }

        Message msg = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(hasContent ? content : null)
                .isRead(false)
                .build();
        messageRepository.save(msg);

        if (hasFiles) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                String stored = fileStorageService.store(file);
                if (stored == null) continue;
                Attachment a = Attachment.builder()
                        .message(msg)
                        .originalName(file.getOriginalFilename())
                        .storedPath(stored)
                        .mimeType(file.getContentType())
                        .sizeBytes(file.getSize())
                        .build();
                attachmentRepository.save(a);
            }
        }
    }

    @Transactional
    public void markRead(User currentUser, Long withUserId) {
        User other = userRepository.findById(withUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь", withUserId));
        messageRepository.findConversation(currentUser, other).stream()
                .filter(m -> m.getReceiver().getId().equals(currentUser.getId()) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });
    }

    public long countUnread(User user) {
        return messageRepository.findSendersByReceiver(user).stream()
                .mapToLong(sender -> messageRepository
                        .countBySenderAndReceiverAndIsReadFalse(sender, user))
                .sum();
    }
}
