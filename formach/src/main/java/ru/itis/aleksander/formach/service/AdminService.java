package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.aleksander.formach.dto.response.UserResponse;
import ru.itis.aleksander.formach.entity.Complaint;
import ru.itis.aleksander.formach.entity.ComplaintStatus;
import ru.itis.aleksander.formach.entity.NotificationType;
import ru.itis.aleksander.formach.entity.Role;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.exсeption.NotFoundException;
import ru.itis.aleksander.formach.repository.ComplaintRepository;
import ru.itis.aleksander.formach.repository.PostRepository;
import ru.itis.aleksander.formach.repository.TopicRepository;
import ru.itis.aleksander.formach.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final PostRepository postRepository;
    private final ComplaintRepository complaintRepository;
    private final TopicService topicService;
    private final PostService postService;
    private final NotificationService notificationService;

    public Page<UserResponse> getUsers(String search, Pageable pageable) {
        return userService.findAll(search, pageable);
    }

    @Transactional
    public void setRole(Long id, Role role) {
        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("Назначить ADMIN нельзя — админ один");
        }
        User u = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь", id));
        if (u.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Нельзя менять роль администратора");
        }
        u.setRole(role);
        userRepository.save(u);
        log.info("Роль пользователя {} изменена на {}", u.getLogin(), role);
    }

    @Transactional
    public void banUser(User actor,
                        Long targetId,
                        String reason,
                        LocalDateTime bannedUntil,
                        List<MultipartFile> proofs) {
        userService.banUser(actor,targetId, reason, bannedUntil, proofs);
    }

    @Transactional
    public void unbanUser(Long id) {
        userService.unbanUser(id);
    }

    public Page<Complaint> getComplaints(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            return complaintRepository.findByStatusOrderByCreatedAtDesc(
                    ComplaintStatus.valueOf(status), pageable);
        }
        return complaintRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Complaint getComplaint(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Жалоба", id));
    }

    @Transactional
    public void resolveComplaint(Long id, String resolution) {
        Complaint c = complaintRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Жалоба", id));
        c.setStatus(ComplaintStatus.REVIEWED);
        c.setResolution(resolution);
        c.setResolvedAt(LocalDateTime.now());
        complaintRepository.save(c);
        notifyAuthor(c, "Ваша жалоба рассмотрена и принята.", resolution);
        log.info("Жалоба {} одобрена", id);
    }

    private void notifyAuthor(Complaint c, String headline, String resolution) {
        StringBuilder msg = new StringBuilder(headline);
        if (resolution != null && !resolution.isBlank()) {
            msg.append(" Ответ админа: ").append(resolution);
        }
        notificationService.notify(c.getAuthor(), null,
                NotificationType.COMPLAINT_ON_YOU,
                msg.toString(),
                "/admin/complaints/" + c.getId());
    }

    @Transactional
    public void resolveComplaintWithActions(User actor, Long id,
                                            String resolution,
                                            boolean deletePost,
                                            boolean deleteTopic,
                                            boolean banAuthor,
                                            String banReason,
                                            LocalDateTime bannedUntil) {
        Complaint c = complaintRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Жалоба", id));

        StringBuilder summary = new StringBuilder();
        if (deletePost && c.getPost() != null) {
            try {
                postService.forceDelete(c.getPost().getId());
                summary.append(" Пост удалён.");
            } catch (Exception ex) {
                log.warn("Не удалось удалить пост #{}: {}", c.getPost().getId(), ex.getMessage());
            }
        }
        if (deleteTopic && c.getTopic() != null) {
            try {
                topicService.forceDelete(c.getTopic().getId());
                summary.append(" Тема удалена.");
            } catch (Exception ex) {
                log.warn("Не удалось удалить тему #{}: {}", c.getTopic().getId(), ex.getMessage());
            }
        }
        if (banAuthor && c.getReportedUser() != null) {
            userService.banUser(actor, c.getReportedUser().getId(), banReason, bannedUntil);
            summary.append(" Пользователь заблокирован.");
        }

        c.setStatus(ComplaintStatus.REVIEWED);
        c.setResolution(resolution);
        c.setResolvedAt(LocalDateTime.now());
        complaintRepository.save(c);

        notifyAuthor(c, "Ваша жалоба рассмотрена." + summary, resolution);
        log.info("Жалоба {} одобрена с действиями: delPost={}, delTopic={}, ban={}",
                id, deletePost, deleteTopic, banAuthor);
    }

    @Transactional
    public void rejectComplaint(Long id, String resolution) {
        Complaint c = complaintRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Жалоба", id));
        c.setStatus(ComplaintStatus.REJECTED);
        c.setResolution(resolution);
        c.setResolvedAt(LocalDateTime.now());
        complaintRepository.save(c);
        notifyAuthor(c, "Ваша жалоба отклонена — нарушения не подтверждены.", resolution);
        log.info("Жалоба {} отклонена", id);
    }

    @Transactional
    public void deleteTopic(Long id) {
        topicRepository.findById(id).ifPresent(topic -> {
            topicService.forceDelete(id);
            log.info("Админ удалил тему {}", id);
        });
    }

    @Transactional
    public void setPinned(Long id, boolean pinned) {
        topicService.setPinnedByAdmin(id, pinned);
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.findById(id).ifPresent(post -> {
            postService.forceDelete(id);
            log.info("Админ удалил пост {}", id);
        });
    }

    public AdminDashboard getDashboard() {
        return new AdminDashboard(
                userRepository.count(),
                topicRepository.count(),
                postRepository.count(),
                complaintRepository.countByStatus(ComplaintStatus.PENDING)
        );
    }

    public record AdminDashboard(long users, long topics, long posts, long pendingComplaints) {}
}
