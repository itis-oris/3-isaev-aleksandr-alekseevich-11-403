package ru.itis.aleksander.formach.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class SendMessageRequest {

    @Size(max = 2000, message = "Максимум 2000 символов")
    private String content;

    private List<MultipartFile> attachments;
}
