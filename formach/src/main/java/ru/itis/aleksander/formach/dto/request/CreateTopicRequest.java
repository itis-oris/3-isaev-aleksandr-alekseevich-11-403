package ru.itis.aleksander.formach.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.aleksander.formach.entity.TopicType;

import java.util.List;

@Data
public class CreateTopicRequest {

    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 5, max = 200, message = "Заголовок от 5 до 200 символов")
    private String title;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 10, message = "Описание минимум 10 символов")
    private String description;

    @NotNull(message = "Выберите тип темы")
    private TopicType type;

    private List<Long> tagIds;
    private String newTags;

    private List<MultipartFile> attachments;
}
