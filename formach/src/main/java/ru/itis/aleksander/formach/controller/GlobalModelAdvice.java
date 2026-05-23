package ru.itis.aleksander.formach.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.MessageService;
import ru.itis.aleksander.formach.service.NotificationService;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final MessageService messageService;
    private final NotificationService notificationService;

    @ModelAttribute("currentUser")
    public User currentUser() {
        return resolveCurrentUser();
    }

    @ModelAttribute("unreadMessages")
    public long unreadMessages() {
        User me = resolveCurrentUser();
        if (me == null) return 0L;
        try {
            return messageService.countUnread(me);
        } catch (Exception e) {
            return 0L;
        }
    }

    @ModelAttribute("unreadNotifications")
    public long unreadNotifications() {
        User me = resolveCurrentUser();
        if (me == null) return 0L;
        try {
            return notificationService.countUnread(me);
        } catch (Exception e) {
            return 0L;
        }
    }

    private User resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof SecurityUser su) {
            return su.getUser();
        }
        return null;
    }
}
