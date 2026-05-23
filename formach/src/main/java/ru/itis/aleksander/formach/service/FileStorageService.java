package ru.itis.aleksander.formach.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/webm", "video/ogg", "video/quicktime",
            "application/pdf",
            "text/plain",
            "application/zip",
            "application/x-zip-compressed"
    );

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload.dir}") String dir) {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузок: " + uploadDir, e);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Недопустимый тип файла: " + contentType);
        }

        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : "";
        String filename = UUID.randomUUID() + ext;

        try {
            file.transferTo(uploadDir.resolve(filename));
            log.info("Файл сохранён: {}", filename);
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения файла", e);
        }
    }

    public void delete(String path) {
        if (path == null) return;
        try {
            String filename = path.replace("/uploads/", "");
            Files.deleteIfExists(uploadDir.resolve(filename));
        } catch (IOException e) {
            log.warn("Не удалось удалить файл: {}", path);
        }
    }
}
