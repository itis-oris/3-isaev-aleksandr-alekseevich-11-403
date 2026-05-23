package ru.itis.aleksander.formach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private String url;
    private String originalName;
    private String mimeType;
    private Long sizeBytes;
}
