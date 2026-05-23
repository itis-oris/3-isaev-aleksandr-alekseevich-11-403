package ru.itis.aleksander.formach.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.itis.aleksander.formach.service.BanAppealService;

@Controller
@RequestMapping("/appeal")
@RequiredArgsConstructor
public class BanAppealController {

    private final BanAppealService banAppealService;

    @GetMapping
    public String form(@RequestParam(required = false) String login, Model model) {
        model.addAttribute("login", login);
        return "appeal/new";
    }

    @PostMapping
    public String submit(@RequestParam String login,
                         @RequestParam(required = false) String email,
                         @RequestParam String message,
                         Model model) {
        try {
            banAppealService.submit(login, email, message);
        } catch (IllegalArgumentException e) {
            model.addAttribute("login", login);
            model.addAttribute("email", email);
            model.addAttribute("message", message);
            model.addAttribute("error", e.getMessage());
            return "appeal/new";
        }
        return "appeal/success";
    }
}
