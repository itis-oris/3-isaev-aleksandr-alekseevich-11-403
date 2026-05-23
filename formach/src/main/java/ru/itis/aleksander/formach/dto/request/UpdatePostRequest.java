package ru.itis.aleksander.formach.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UpdatePostRequest {

    @Size(max = 10000, message = "Максимум 10000 символов")
    private String content;

    private List<MultipartFile> newAttachments;

    private List<Long> removeAttachmentIds;
}
