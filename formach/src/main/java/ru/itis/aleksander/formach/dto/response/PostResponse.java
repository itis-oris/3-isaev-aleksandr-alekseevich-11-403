package ru.itis.aleksander.formach.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponse {

    private Long id;
    private String content;
    private String authorLogin;
    private Long authorId;
    private Long topicId;
    private Long topicAuthorId;
    private Boolean pinned;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private long likeCount;
    private boolean likedByCurrentUser;
    private boolean likedByTopicAuthor;
    private boolean isBestAnswer;
    private Long parentId;
    private String parentAuthorLogin;
    private List<PostResponse> replies;
    private List<AttachmentResponse> attachments;
}