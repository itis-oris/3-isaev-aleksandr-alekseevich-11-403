package ru.itis.aleksander.formach.dto.response;

import lombok.Data;
import ru.itis.aleksander.formach.entity.TopicType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class TopicResponse {

    private Long id;
    private String title;
    private String description;
    private TopicType type;
    private String authorLogin;
    private Long authorId;
    private LocalDateTime createdAt;
    private Boolean isClosed;
    private Long viewCount;
    private Set<String> tags;
    private List<Long> tagIds;
    private int postCount;
    private Boolean pinnedByAdmin;
    private List<AttachmentResponse> attachments;
}