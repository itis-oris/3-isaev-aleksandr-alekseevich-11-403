package ru.itis.aleksander.formach.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.NotificationService;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public String list(@AuthenticationPrincipal SecurityUser principal,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("notifications",
                notificationService.list(principal.getUser(), page, 30));
        notificationService.markAllRead(principal.getUser());
        return "notifications/list";
    }

    @PostMapping("/read-all")
    public String markAllRead(@AuthenticationPrincipal SecurityUser principal) {
        notificationService.markAllRead(principal.getUser());
        return "redirect:/notifications";
    }
}
