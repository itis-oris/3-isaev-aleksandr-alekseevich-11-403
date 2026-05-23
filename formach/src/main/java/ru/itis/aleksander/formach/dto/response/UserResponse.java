package ru.itis.aleksander.formach.dto.response;

import lombok.Data;
import ru.itis.aleksander.formach.entity.Gender;
import ru.itis.aleksander.formach.entity.Role;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String login;
    private String email;
    private String firstName;
    private String lastName;
    private Gender gender;
    private Role role;
    private Boolean isBanned;
    private String banReason;
    private LocalDateTime bannedUntil;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private String avatarPath;
    private long topicCount;
    private long postCount;
    private long likesReceived;
}