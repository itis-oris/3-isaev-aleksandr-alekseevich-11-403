package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.aleksander.formach.dto.request.CreateComplaintRequest;
import ru.itis.aleksander.formach.entity.Attachment;
import ru.itis.aleksander.formach.entity.Complaint;
import ru.itis.aleksander.formach.entity.ComplaintStatus;
import ru.itis.aleksander.formach.entity.Post;
import ru.itis.aleksander.formach.entity.Topic;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.exсeption.NotFoundException;
import ru.itis.aleksander.formach.repository.AttachmentRepository;
import ru.itis.aleksander.formach.repository.ComplaintRepository;
import ru.itis.aleksander.formach.repository.PostRepository;
import ru.itis.aleksander.formach.repository.TopicRepository;
import ru.itis.aleksander.formach.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final PostRepository postRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public Complaint create(CreateComplaintRequest request, User author,
                            List<MultipartFile> proofs) {
        if (request.getPostId() == null
                && request.getTopicId() == null
                && request.getReportedUserId() == null) {
            throw new IllegalArgumentException("Не указано, на кого жалоба");
        }

        Post post = null;
        Topic topic = null;
        User reported = null;

        if (request.getPostId() != null) {
            post = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new NotFoundException("Пост", request.getPostId()));
            reported = post.getAuthor();
        }
        if (request.getTopicId() != null) {
            topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new NotFoundException("Тема", request.getTopicId()));
            if (reported == null) reported = topic.getAuthor();
        }
        if (request.getReportedUserId() != null) {
            reported = userRepository.findById(request.getReportedUserId())
                    .orElseThrow(() -> new NotFoundException("Пользователь", request.getReportedUserId()));
        }

        if (reported != null && reported.getId().equals(author.getId())) {
            throw new IllegalArgumentException("Нельзя пожаловаться на самого себя");
        }

        Complaint complaint = Complaint.builder()
                .reason(request.getReason())
                .author(author)
                .post(post)
                .topic(topic)
                .reportedUser(reported)
                .status(ComplaintStatus.PENDING)
                .build();

        complaintRepository.save(complaint);

        if (proofs != null) {
            for (MultipartFile file : proofs) {
                if (file == null || file.isEmpty()) continue;
                String stored = fileStorageService.store(file);
                if (stored == null) continue;
                Attachment a = Attachment.builder()
                        .complaint(complaint)
                        .originalName(file.getOriginalFilename())
                        .storedPath(stored)
                        .mimeType(file.getContentType())
                        .sizeBytes(file.getSize())
                        .build();
                attachmentRepository.save(a);
            }
        }

        log.info("Жалоба #{} от {} (post={}, topic={}, user={})",
                complaint.getId(), author.getLogin(),
                post != null ? post.getId() : "-",
                topic != null ? topic.getId() : "-",
                reported != null ? reported.getLogin() : "-");
        return complaint;
    }
}
