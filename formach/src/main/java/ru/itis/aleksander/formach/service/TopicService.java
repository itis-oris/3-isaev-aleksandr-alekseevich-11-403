package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.aleksander.formach.dto.request.CreateTopicRequest;
import ru.itis.aleksander.formach.dto.request.UpdateTopicRequest;
import ru.itis.aleksander.formach.dto.response.AttachmentResponse;
import ru.itis.aleksander.formach.dto.response.TopicResponse;
import ru.itis.aleksander.formach.entity.Attachment;
import ru.itis.aleksander.formach.entity.Tag;
import ru.itis.aleksander.formach.entity.Topic;
import ru.itis.aleksander.formach.entity.TopicType;
import ru.itis.aleksander.formach.entity.TopicView;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.exсeption.AccessDeniedException;
import ru.itis.aleksander.formach.exсeption.NotFoundException;
import ru.itis.aleksander.formach.repository.AttachmentRepository;
import ru.itis.aleksander.formach.repository.BookmarkRepository;
import ru.itis.aleksander.formach.repository.ComplaintRepository;
import ru.itis.aleksander.formach.repository.TopicRepository;
import ru.itis.aleksander.formach.repository.TopicViewRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicService {

    private final TopicRepository topicRepository;
    private final TagService tagService;
    private final TopicViewRepository topicViewRepository;
    private final FileStorageService fileStorageService;
    private final AttachmentRepository attachmentRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ComplaintRepository complaintRepository;

    public Page<TopicResponse> getAll(Pageable pageable) {
        return topicRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<TopicResponse> search(String search, List<Long> tagIds, String status,
                                      String sort, int page, int size) {
        String q = search == null ? "" : search.trim();
        String s = sort == null ? "new" : sort;
        String st = status == null ? "" : status;
        boolean hasTags = tagIds != null && !tagIds.isEmpty();

        Sort sortBy = switch (s) {
            case "popular" -> Sort.by("viewCount").descending();
            default -> Sort.by("createdAt").descending();
        };
        Sort full = Sort.by("pinnedByAdmin").descending().and(sortBy);
        PageRequest pr = PageRequest.of(page, size, full);

        if ("solved".equals(st)) {
            return topicRepository.findSolved(q, TopicType.QUESTION, pr).map(this::toResponse);
        }
        if ("unsolved".equals(st)) {
            return topicRepository.findUnsolved(q, TopicType.QUESTION, pr).map(this::toResponse);
        }
        if ("info".equals(st)) {
            return topicRepository.findByType(q, TopicType.INFO, pr).map(this::toResponse);
        }

        if ("discussed".equals(s) && !hasTags) {
            return topicRepository
                    .searchOrderByPosts(q, PageRequest.of(page, size))
                    .map(this::toResponse);
        }

        if (hasTags) {
            return topicRepository.findByAllTags(q, tagIds, tagIds.size(), pr)
                    .map(this::toResponse);
        }
        return topicRepository.searchByTitle(q, pr).map(this::toResponse);
    }

    public List<TopicResponse> topicsCreatedAfter(LocalDateTime after, int limit) {
        return topicRepository.advancedSearch(null, null, false, after)
                .stream()
                .limit(limit)
                .map(this::toResponse)
                .toList();
    }

    public List<TopicResponse> recentFromAuthors(List<User> authors, int limit) {
        if (authors == null || authors.isEmpty()) return List.of();
        return topicRepository
                .findByAuthorInRecent(authors, PageRequest.of(0, limit))
                .stream().map(this::toResponse).toList();
    }

    public Page<TopicResponse> findByAuthor(User author, int page, int size) {
        return topicRepository.findByAuthor(author,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::toResponse);
    }

    @Transactional
    public TopicResponse create(CreateTopicRequest request, User author) {
        Set<Tag> tags = tagService.resolveAll(request.getTagIds(), request.getNewTags());

        Topic topic = Topic.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .author(author)
                .isClosed(false)
                .viewCount(0L)
                .tags(tags)
                .build();

        topicRepository.save(topic);
        saveAttachmentsForTopic(topic, request.getAttachments());
        log.info("Создана тема: {} автором {}", topic.getTitle(), author.getLogin());
        return toResponse(topic);
    }

    private void saveAttachmentsForTopic(Topic topic, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String stored = fileStorageService.store(file);
            if (stored == null) continue;
            Attachment a = Attachment.builder()
                    .topic(topic)
                    .originalName(file.getOriginalFilename())
                    .storedPath(stored)
                    .mimeType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .build();
            attachmentRepository.save(a);
        }
    }

    @Transactional
    public TopicResponse getById(Long id, User currentUser) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Тема", id));

        if (currentUser != null) {
            var existing = topicViewRepository.findByTopicAndUser(topic, currentUser);
            if (existing.isPresent()) {
                TopicView tv = existing.get();
                tv.setViewedAt(LocalDateTime.now());
                topicViewRepository.save(tv);
            } else {
                topicViewRepository.save(TopicView.builder()
                        .topic(topic)
                        .user(currentUser)
                        .build());
                topic.setViewCount(topic.getViewCount() + 1);
                topicRepository.save(topic);
            }
        }

        return toResponse(topic);
    }

    public TopicResponse update(Long id, UpdateTopicRequest request, User currentUser) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Тема", id));

        if (!topic.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Только автор может редактировать тему");
        }

        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        topic.setTags(tagService.resolveAll(request.getTagIds(), request.getNewTags()));

        if (request.getRemoveAttachmentIds() != null) {
            for (Long aId : request.getRemoveAttachmentIds()) {
                attachmentRepository.findById(aId).ifPresent(a -> {
                    if (a.getTopic() != null && a.getTopic().getId().equals(topic.getId())) {
                        fileStorageService.delete(a.getStoredPath());
                        attachmentRepository.delete(a);
                        if (topic.getAttachments() != null) {
                            topic.getAttachments().remove(a);
                        }
                    }
                });
            }
        }
        saveAttachmentsForTopic(topic, request.getNewAttachments());

        topicRepository.save(topic);
        log.info("Обновлена тема: {}", topic.getTitle());
        return toResponse(topic);
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Тема", id));

        if (!topic.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Только автор может удалить тему");
        }

        deleteTopicFiles(topic);
        bookmarkRepository.deleteByTopic(topic);
        complaintRepository.deleteByTopic(topic);
        topicViewRepository.deleteByTopic(topic);

        topic.setBestAnswer(null);
        topicRepository.saveAndFlush(topic);

        topicRepository.delete(topic);
        log.info("Удалена тема: {}", topic.getTitle());
    }

    private void deleteTopicFiles(Topic topic) {
        if (topic.getAttachments() != null) {
            topic.getAttachments().forEach(a -> fileStorageService.delete(a.getStoredPath()));
        }
        if (topic.getPosts() != null) {
            topic.getPosts().forEach(p -> {
                if (p.getAttachments() != null) {
                    p.getAttachments().forEach(a -> fileStorageService.delete(a.getStoredPath()));
                }
            });
        }
    }

    @Transactional
    public void forceDelete(Long id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Тема", id));
        deleteTopicFiles(topic);
        bookmarkRepository.deleteByTopic(topic);
        complaintRepository.deleteByTopic(topic);
        topicViewRepository.deleteByTopic(topic);
        topic.setBestAnswer(null);
        topicRepository.saveAndFlush(topic);
        topicRepository.delete(topic);
        log.info("Принудительно удалена тема: {}", topic.getTitle());
    }

    @Transactional
    public void close(Long id, User currentUser) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Тема", id));

        if (!topic.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Только автор может закрыть тему");
        }

        topic.setIsClosed(true);
        topicRepository.save(topic);
    }

    @Transactional
    public void open(Long id, User currentUser) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Тема", id));

        if (!topic.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Только автор может открыть тему");
        }

        topic.setIsClosed(false);
        topicRepository.save(topic);
    }

    public TopicResponse toResponse(Topic topic) {
        TopicResponse response = new TopicResponse();
        response.setId(topic.getId());
        response.setTitle(topic.getTitle());
        response.setDescription(topic.getDescription());
        response.setType(topic.getType());
        response.setAuthorLogin(topic.getAuthor().getLogin());
        response.setAuthorId(topic.getAuthor().getId());
        response.setCreatedAt(topic.getCreatedAt());
        response.setIsClosed(topic.getIsClosed());
        response.setViewCount(topic.getViewCount());
        response.setTags(topic.getTags() != null
                ? topic.getTags().stream().map(Tag::getName).collect(Collectors.toSet())
                : Collections.emptySet());
        response.setPostCount(topic.getPosts() != null
                ? topic.getPosts().size()
                : 0);
        response.setTagIds(topic.getTags() != null
                ? topic.getTags().stream().map(Tag::getId).collect(Collectors.toList())
                : Collections.emptyList());
        response.setAttachments(topic.getAttachments() == null ? Collections.emptyList()
                : topic.getAttachments().stream()
                    .map(a -> new AttachmentResponse(a.getId(), a.getStoredPath(),
                            a.getOriginalName(), a.getMimeType(), a.getSizeBytes()))
                    .toList());
        response.setPinnedByAdmin(topic.getPinnedByAdmin());
        return response;
    }

    @Transactional
    public void setPinnedByAdmin(Long id, boolean pinned) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Тема", id));
        topic.setPinnedByAdmin(pinned);
        topicRepository.save(topic);
        log.info("Тема {} {} админом", topic.getTitle(), pinned ? "закреплена" : "откреплена");
    }
}
