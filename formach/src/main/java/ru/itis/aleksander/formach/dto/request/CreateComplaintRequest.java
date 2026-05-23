package ru.itis.aleksander.formach.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateComplaintRequest {

    private Long postId;

    private Long topicId;

    private Long reportedUserId;

    @NotBlank(message = "Опишите причину жалобы")
    @Size(min = 5, max = 1000, message = "Причина от 5 до 1000 символов")
    private String reason;
}
