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
import ru.itis.aleksander.formach.service.PasswordChangeService;

@Controller
@RequestMapping("/profile/password")
@RequiredArgsConstructor
public class PasswordChangeController {

    private final PasswordChangeService passwordChangeService;

    @GetMapping
    public String page(@AuthenticationPrincipal SecurityUser principal, Model model) {
        model.addAttribute("email", principal.getUser().getEmail());
        return "profile/password";
    }

    @PostMapping("/request")
    public String requestCode(@AuthenticationPrincipal SecurityUser principal,
                              Model model) {
        passwordChangeService.requestCode(principal.getUser());
        model.addAttribute("email", principal.getUser().getEmail());
        model.addAttribute("requested", true);
        return "profile/password";
    }

    @PostMapping("/confirm")
    public String confirm(@RequestParam String code,
                          @RequestParam String newPassword,
                          @AuthenticationPrincipal SecurityUser principal,
                          Model model) {
        try {
            passwordChangeService.confirm(principal.getUser(), code, newPassword);
        } catch (IllegalArgumentException e) {
            model.addAttribute("email", principal.getUser().getEmail());
            model.addAttribute("requested", true);
            model.addAttribute("error", e.getMessage());
            return "profile/password";
        }
        model.addAttribute("success", true);
        return "profile/password";
    }
}
