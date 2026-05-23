package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.aleksander.formach.dto.request.CreatePostRequest;
import ru.itis.aleksander.formach.dto.request.UpdatePostRequest;
import ru.itis.aleksander.formach.dto.response.AttachmentResponse;
import ru.itis.aleksander.formach.dto.response.PostResponse;
import ru.itis.aleksander.formach.entity.Attachment;
import ru.itis.aleksander.formach.entity.Like;
import ru.itis.aleksander.formach.entity.NotificationType;
import ru.itis.aleksander.formach.entity.Post;
import ru.itis.aleksander.formach.entity.Topic;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.exсeption.AccessDeniedException;
import ru.itis.aleksander.formach.exсeption.NotFoundException;
import ru.itis.aleksander.formach.repository.AttachmentRepository;
import ru.itis.aleksander.formach.repository.LikeRepository;
import ru.itis.aleksander.formach.repository.PostRepository;
import ru.itis.aleksander.formach.repository.TopicRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final TopicRepository topicRepository;
    private final LikeRepository likeRepository;
    private final FileStorageService fileStorageService;
    private final AttachmentRepository attachmentRepository;
    private final NotificationService notificationService;
    private final MentionService mentionService;

    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Сообщение", id));
    }

    @Transactional
    public PostResponse create(Long topicId, CreatePostRequest request, User author) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Тема", topicId));

        if (topic.getIsClosed()) {
            throw new AccessDeniedException("Тема закрыта");
        }

        boolean hasContent = request.getContent() != null && !request.getContent().isBlank();
        boolean hasFiles = request.getAttachments() != null
                && request.getAttachments().stream().anyMatch(f -> f != null && !f.isEmpty());
        if (!hasContent && !hasFiles) {
            throw new IllegalArgumentException("Сообщение пустое — добавьте текст или хотя бы один файл");
        }

        Post parent = null;
        if (request.getParentId() != null) {
            parent = postRepository.findById(request.getParentId())
                    .orElseThrow(() -> new NotFoundException("Сообщение", request.getParentId()));
        }

        Post post = Post.builder()
                .content(hasContent ? request.getContent() : null)
                .author(author)
                .topic(topic)
                .parent(parent)
                .isPinned(false)
                .build();

        postRepository.save(post);
        saveAttachmentsForPost(post, request.getAttachments());

        String link = "/topics/" + topic.getId() + "#post-" + post.getId();
        if (parent != null) {
            notificationService.notify(parent.getAuthor(), author,
                    NotificationType.REPLY_TO_POST,
                    "@" + author.getLogin() + " ответил(а) на ваш пост в «" + topic.getTitle() + "»",
                    link);
        }
        notificationService.notify(topic.getAuthor(), author,
                NotificationType.REPLY_TO_TOPIC,
                "@" + author.getLogin() + " написал(а) в вашей теме «" + topic.getTitle() + "»",
                link);

        for (User mentioned : mentionService.extract(post.getContent())) {
            notificationService.notify(mentioned, author,
                    NotificationType.MENTION,
                    "@" + author.getLogin() + " упомянул(а) вас в «" + topic.getTitle() + "»",
                    link);
        }

        log.info("Создан пост в теме {} автором {}", topic.getTitle(), author.getLogin());
        return toResponse(post, author);
    }

    private void saveAttachmentsForPost(Post post, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String stored = fileStorageService.store(file);
            if (stored == null) continue;
            Attachment a = Attachment.builder()
                    .post(post)
                    .originalName(file.getOriginalFilename())
                    .storedPath(stored)
                    .mimeType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .build();
            attachmentRepository.save(a);
        }
    }

    @Transactional
    public PostResponse update(Long postId, UpdatePostRequest request, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Сообщение", postId));

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Только автор может редактировать сообщение");
        }

        boolean hasContent = request.getContent() != null && !request.getContent().isBlank();
        post.setContent(hasContent ? request.getContent() : null);
        post.setEditedAt(LocalDateTime.now());

        if (request.getRemoveAttachmentIds() != null) {
            for (Long aId : request.getRemoveAttachmentIds()) {
                attachmentRepository.findById(aId).ifPresent(a -> {
                    if (a.getPost() != null && a.getPost().getId().equals(post.getId())) {
                        fileStorageService.delete(a.getStoredPath());
                        attachmentRepository.delete(a);
                        if (post.getAttachments() != null) {
                            post.getAttachments().remove(a);
                        }
                    }
                });
            }
        }

        long remainingAttachments =
                post.getAttachments() == null ? 0 : post.getAttachments().size();
        boolean hasNewFiles = request.getNewAttachments() != null
                && request.getNewAttachments().stream().anyMatch(f -> f != null && !f.isEmpty());
        if (!hasContent && remainingAttachments == 0 && !hasNewFiles) {
            throw new IllegalArgumentException("Сообщение пустое — оставьте текст или хотя бы один файл");
        }

        saveAttachmentsForPost(post, request.getNewAttachments());

        postRepository.save(post);
        log.info("Обновлён пост {}", postId);
        return toResponse(post, currentUser);
    }

    @Transactional
    public void delete(Long postId, User currentUser) {
        Post post = findById(postId);

        boolean isAuthor = post.getAuthor().getId().equals(currentUser.getId());
        boolean isTopicAuthor = post.getTopic().getAuthor().getId().equals(currentUser.getId());

        if (!isAuthor && !isTopicAuthor) {
            throw new AccessDeniedException("Нет прав на удаление сообщения");
        }

        deletePostFiles(post);
        postRepository.delete(post);
        log.info("Удалён пост {}", postId);
    }

    @Transactional
    public void forceDelete(Long postId) {
        Post post = findById(postId);
        deletePostFiles(post);
        postRepository.delete(post);
        log.info("Принудительно удалён пост {}", postId);
    }

    private void deletePostFiles(Post post) {
        if (post.getAttachments() != null) {
            post.getAttachments().forEach(a -> fileStorageService.delete(a.getStoredPath()));
        }
    }

    @Transactional
    public void togglePin(Long postId, User currentUser) {
        Post post = findById(postId);

        if (!post.getTopic().getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Только автор темы может закреплять сообщения");
        }

        post.setIsPinned(!post.getIsPinned());
        postRepository.save(post);
    }

    @Transactional
    public void toggleLike(Long postId, User currentUser) {
        Post post = findById(postId);

        if (likeRepository.existsByPostAndUser(post, currentUser)) {
            likeRepository.deleteByPostAndUser(post, currentUser);
        } else {
            likeRepository.save(Like.builder()
                    .post(post)
                    .user(currentUser)
                    .build());
            notificationService.notify(post.getAuthor(), currentUser,
                    NotificationType.LIKE,
                    "@" + currentUser.getLogin() + " лайкнул(а) ваш пост",
                    "/topics/" + post.getTopic().getId() + "#post-" + post.getId());
        }
        log.info("Лайк переключён на посте {} пользователем {}", postId, currentUser.getLogin());
    }

    @Transactional
    public void setBestAnswer(Long postId, User currentUser) {
        Post post = findById(postId);

        Topic topic = post.getTopic();

        if (!topic.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Только автор вопроса может выбрать лучший ответ");
        }

        if (topic.getBestAnswer() != null
                && topic.getBestAnswer().getId().equals(postId)) {
            topic.setBestAnswer(null);
        } else {
            topic.setBestAnswer(post);
            notificationService.notify(post.getAuthor(), currentUser,
                    NotificationType.BEST_ANSWER,
                    "Ваш ответ выбран лучшим в «" + topic.getTitle() + "»",
                    "/topics/" + topic.getId() + "#post-" + post.getId());
        }

        topicRepository.save(topic);
    }

    public Page<PostResponse> getByAuthor(
            User author, int page, int size, User currentUser) {
        return postRepository.findByAuthorOrderByCreatedAtDesc(author,
                PageRequest.of(page, size))
                .map(p -> toResponse(p, currentUser));
    }

    public List<PostResponse> getByTopic(Long topicId, User currentUser) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Тема", topicId));

        List<Post> posts = postRepository
                .findByTopicAndParentIsNullOrderByIsPinnedDescCreatedAtAsc(topic);

        return posts.stream()
                .map(post -> toResponseWithReplies(post, currentUser))
                .collect(Collectors.toList());
    }

    private PostResponse toResponseWithReplies(Post post, User currentUser) {
        PostResponse response = toResponse(post, currentUser);

        if (post.getReplies() != null && !post.getReplies().isEmpty()) {
            response.setReplies(post.getReplies().stream()
                    .sorted(Comparator.comparing(Post::getCreatedAt))
                    .map(reply -> toResponseWithReplies(reply, currentUser))
                    .collect(Collectors.toList()));
        }

        return response;
    }

    public PostResponse toResponse(Post post, User currentUser) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setAuthorLogin(post.getAuthor().getLogin());
        response.setAuthorId(post.getAuthor().getId());
        response.setTopicId(post.getTopic().getId());
        response.setTopicAuthorId(post.getTopic().getAuthor().getId());
        response.setPinned(post.getIsPinned());
        response.setCreatedAt(post.getCreatedAt());
        response.setEditedAt(post.getEditedAt());
        response.setLikeCount(likeRepository.countByPost(post));
        if (post.getParent() != null) {
            response.setParentId(post.getParent().getId());
            response.setParentAuthorLogin(post.getParent().getAuthor().getLogin());
        }
        response.setAttachments(post.getAttachments() == null ? Collections.emptyList()
                : post.getAttachments().stream()
                    .map(a -> new AttachmentResponse(a.getId(), a.getStoredPath(),
                            a.getOriginalName(), a.getMimeType(), a.getSizeBytes()))
                    .toList());

        if (currentUser != null) {
            response.setLikedByCurrentUser(
                    likeRepository.existsByPostAndUser(post, currentUser));
        }

        User topicAuthor = post.getTopic().getAuthor();
        response.setLikedByTopicAuthor(
                likeRepository.existsByPostAndUser(post, topicAuthor));

        Topic topic = post.getTopic();
        response.setBestAnswer(
                topic.getBestAnswer() != null
                        && topic.getBestAnswer().getId().equals(post.getId()));

        return response;
    }
}