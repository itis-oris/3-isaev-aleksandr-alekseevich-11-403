package ru.itis.aleksander.formach.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.aleksander.formach.entity.Attachment;
import ru.itis.aleksander.formach.entity.User;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByBannedUser(User user);
}
