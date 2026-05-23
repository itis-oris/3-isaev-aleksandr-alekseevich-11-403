package ru.itis.aleksander.formach.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String login;

    @Column(nullable = false, length = 30)
    private String firstName;

    @Column(nullable = false, length = 30)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "author")
    private List<Topic> topics;

    @OneToMany(mappedBy = "author")
    private List<Post> posts;

    @Builder.Default
    private Boolean isBanned = false;

    @Column(length = 500)
    private String banReason;

    private LocalDateTime bannedUntil;

    private String avatarPath;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
