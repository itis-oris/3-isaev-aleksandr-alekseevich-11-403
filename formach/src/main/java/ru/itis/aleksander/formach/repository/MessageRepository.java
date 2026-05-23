package ru.itis.aleksander.formach.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.aleksander.formach.entity.Message;
import ru.itis.aleksander.formach.entity.User;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender = :a AND m.receiver = :b)
           OR (m.sender = :b AND m.receiver = :a)
        ORDER BY m.createdAt ASC
        """)
    List<Message> findConversation(@Param("a") User a, @Param("b") User b);

    @Query("SELECT DISTINCT m.receiver FROM Message m WHERE m.sender = :user")
    List<User> findReceiversBySender(@Param("user") User user);

    @Query("SELECT DISTINCT m.sender FROM Message m WHERE m.receiver = :user")
    List<User> findSendersByReceiver(@Param("user") User user);

    long countBySenderAndReceiverAndIsReadFalse(User sender, User receiver);
}
