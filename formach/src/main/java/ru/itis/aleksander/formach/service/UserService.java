package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.aleksander.formach.dto.request.RegisterRequest;
import ru.itis.aleksander.formach.dto.request.UpdateUserRequest;
import ru.itis.aleksander.formach.dto.response.UserResponse;
import ru.itis.aleksander.formach.entity.Role;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.exсeption.AlreadyExistsException;
import ru.itis.aleksander.formach.exсeption.NotFoundException;
import ru.itis.aleksander.formach.entity.Attachment;
import ru.itis.aleksander.formach.entity.NotificationType;
import ru.itis.aleksander.formach.repository.AttachmentRepository;
import ru.itis.aleksander.formach.repository.LikeRepository;
import ru.itis.aleksander.formach.repository.PostRepository;
import ru.itis.aleksander.formach.repository.TopicRepository;
import ru.itis.aleksander.formach.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final TopicRepository topicRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final AttachmentRepository attachmentRepository;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new AlreadyExistsException("login", "Логин", request.getLogin());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("email", "Email", request.getEmail());
        }

        User user = User.builder()
                .login(request.getLogin())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .gender(request.getGender())
                .role(Role.USER)
                .isBanned(false)
                .emailVerified(false)
                .build();

        userRepository.save(user);
        emailVerificationService.sendVerification(user);

        log.info("Зарегистрирован пользователь: {}", user.getLogin());
        return toResponse(user);
    }

    public UserResponse update(Long userId, UpdateUserRequest request) {
        return update(userId, request, null, false);
    }

    @Transactional
    public UserResponse update(Long userId, UpdateUserRequest request,
                               MultipartFile avatar, boolean removeAvatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь", userId));

        if (!user.getLogin().equals(request.getLogin())
                && userRepository.existsByLogin(request.getLogin())) {
            throw new AlreadyExistsException("login", "Логин", request.getLogin());
        }

        user.setLogin(request.getLogin());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setGender(request.getGender());

        if (removeAvatar) {
            fileStorageService.delete(user.getAvatarPath());
            user.setAvatarPath(null);
        } else if (avatar != null && !avatar.isEmpty()) {
            String mime = avatar.getContentType();
            if (mime == null || !(mime.startsWith("image/"))) {
                throw new IllegalArgumentException("Аватар должен быть изображением");
            }
            fileStorageService.delete(user.getAvatarPath());
            user.setAvatarPath(fileStorageService.store(avatar));
        }

        userRepository.save(user);
        log.info("Обновлён профиль пользователя: {}", user.getLogin());
        return toResponse(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь", id));
    }

    public Page<UserResponse> findAll(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return userRepository.findAll(pageable).map(this::toResponse);
        }
        return userRepository.searchUsers(search, pageable).map(this::toResponse);
    }

    @Transactional
    public void banUser(User actor, Long id, String reason, LocalDateTime bannedUntil) {
        banUser(actor, id, reason, bannedUntil, null);
    }

    @Transactional
    public void banUser(User actor, Long id, String reason, LocalDateTime bannedUntil,
                        List<MultipartFile> proofs) {
        User user = findById(id);
        user.setIsBanned(true);
        user.setBanReason(reason != null && !reason.isBlank() ? reason : null);
        user.setBannedUntil(bannedUntil);
        userRepository.save(user);

        deleteBanProofs(user);
        if (proofs != null) {
            for (MultipartFile file : proofs) {
                if (file == null || file.isEmpty()) continue;
                String stored = fileStorageService.store(file);
                if (stored == null) continue;
                Attachment a = Attachment.builder()
                        .bannedUser(user)
                        .originalName(file.getOriginalFilename())
                        .storedPath(stored)
                        .mimeType(file.getContentType())
                        .sizeBytes(file.getSize())
                        .build();
                attachmentRepository.save(a);
            }
        }

        notificationService.notify(user, null,
                NotificationType.BANNED,
                "Вы заблокированы" +
                        (bannedUntil != null ? " до " + bannedUntil.toString().replace("T", " ") : "") +
                        (reason != null && !reason.isBlank() ? ". Причина: " + reason : ""),
                "/login");
        log.info("Пользователь {} заблокирован до {}: {}",
                user.getLogin(), bannedUntil == null ? "бессрочно" : bannedUntil, reason);
    }

    @Transactional
    public void unbanUser(Long id) {
        User user = findById(id);
        user.setIsBanned(false);
        user.setBanReason(null);
        user.setBannedUntil(null);
        userRepository.save(user);
        deleteBanProofs(user);
        log.info("Пользователь {} разблокирован", user.getLogin());
    }

    private void deleteBanProofs(User user) {
        List<Attachment> proofs = attachmentRepository.findByBannedUser(user);
        for (Attachment a : proofs) {
            fileStorageService.delete(a.getStoredPath());
            attachmentRepository.delete(a);
        }
    }

    @Transactional(readOnly = true)
    public List<Attachment> getBanProofsByLogin(String login) {
        if (login == null || login.isBlank()) return List.of();
        return userRepository.findByLogin(login.trim())
                .map(attachmentRepository::findByBannedUser)
                .orElse(List.of());
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setLogin(user.getLogin());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setGender(user.getGender());
        response.setRole(user.getRole());
        response.setIsBanned(user.getIsBanned());
        response.setBanReason(user.getBanReason());
        response.setBannedUntil(user.getBannedUntil());
        response.setEmailVerified(user.getEmailVerified());
        response.setCreatedAt(user.getCreatedAt());
        response.setAvatarPath(user.getAvatarPath());
        return response;
    }

    public UserResponse toResponseWithStats(User user) {
        UserResponse response = toResponse(user);
        response.setTopicCount(topicRepository.countByAuthor(user));
        response.setPostCount(postRepository.countTopLevelByAuthor(user));
        response.setLikesReceived(likeRepository.countReceivedByUser(user));
        return response;
    }

    public boolean isLoginTaken(String login) {
        return userRepository.existsByLogin(login);
    }

    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

}