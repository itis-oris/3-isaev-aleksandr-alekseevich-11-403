package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.aleksander.formach.entity.Attachment;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.entity.WallComment;
import ru.itis.aleksander.formach.entity.WallPost;
import ru.itis.aleksander.formach.exсeption.AccessDeniedException;
import ru.itis.aleksander.formach.exсeption.NotFoundException;
import ru.itis.aleksander.formach.repository.AttachmentRepository;
import ru.itis.aleksander.formach.repository.WallCommentRepository;
import ru.itis.aleksander.formach.repository.WallPostRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WallPostService {

    private final WallPostRepository wallPostRepository;
    private final WallCommentRepository wallCommentRepository;
    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public WallPost publish(User owner, User author, String content, List<MultipartFile> files) {
        if (!owner.getId().equals(author.getId())) {
            throw new AccessDeniedException("Публиковать на чужой стене пока нельзя");
        }
        boolean hasContent = content != null && !content.isBlank();
        boolean hasFiles = files != null && files.stream().anyMatch(f -> f != null && !f.isEmpty());
        if (!hasContent && !hasFiles) {
            throw new IllegalArgumentException("Пустая запись — добавьте текст или хотя бы один файл");
        }
        WallPost post = WallPost.builder()
                .owner(owner)
                .author(author)
                .content(hasContent ? content : null)
                .build();
        wallPostRepository.save(post);

        if (hasFiles) {
            for (MultipartFile f : files) {
                if (f == null || f.isEmpty()) continue;
                String stored = fileStorageService.store(f);
                if (stored == null) continue;
                attachmentRepository.save(Attachment.builder()
                        .wallPost(post)
                        .originalName(f.getOriginalFilename())
                        .storedPath(stored)
                        .mimeType(f.getContentType())
                        .sizeBytes(f.getSize())
                        .build());
            }
        }
        log.info("Запись #{} на стене @{}", post.getId(), owner.getLogin());
        return post;
    }

    @Transactional(readOnly = true)
    public Page<WallPost> wallOf(User owner, int page, int size) {
        return wallPostRepository.findByOwnerOrderByCreatedAtDesc(owner,
                PageRequest.of(page, size));
    }

    @Transactional
    public WallComment comment(Long wallPostId, User author, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Комментарий не может быть пустым");
        }
        WallPost post = wallPostRepository.findById(wallPostId)
                .orElseThrow(() -> new NotFoundException("Запись", wallPostId));
        WallComment c = WallComment.builder()
                .wallPost(post)
                .author(author)
                .content(content.trim())
                .build();
        return wallCommentRepository.save(c);
    }

    @Transactional
    public Long deleteComment(Long commentId, User currentUser) {
        WallComment c = wallCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий", commentId));
        WallPost post = c.getWallPost();
        boolean isAuthor = c.getAuthor().getId().equals(currentUser.getId());
        boolean isPostAuthor = post.getAuthor().getId().equals(currentUser.getId());
        boolean isWallOwner = post.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null
                && currentUser.getRole().name().equals("ADMIN");
        if (!isAuthor && !isPostAuthor && !isWallOwner && !isAdmin) {
            throw new AccessDeniedException("Нет прав на удаление комментария");
        }
        Long ownerId = post.getOwner().getId();
        wallCommentRepository.delete(c);
        return ownerId;
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        WallPost post = wallPostRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запись", id));
        boolean isAuthor = post.getAuthor().getId().equals(currentUser.getId());
        boolean isOwner = post.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null
                && currentUser.getRole().name().equals("ADMIN");
        if (!isAuthor && !isOwner && !isAdmin) {
            throw new AccessDeniedException("Нет прав на удаление");
        }

        if (post.getAttachments() != null) {
            post.getAttachments().forEach(a -> fileStorageService.delete(a.getStoredPath()));
        }
        wallPostRepository.delete(post);
        log.info("Удалена запись #{} со стены @{}", id, post.getOwner().getLogin());
    }
}
