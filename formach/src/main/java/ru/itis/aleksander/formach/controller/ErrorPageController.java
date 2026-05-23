package ru.itis.aleksander.formach.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

    @GetMapping("/error/403")
    public String forbidden(Model model) {
        model.addAttribute("message", "У вас нет прав для этого действия.");
        return "error/403";
    }

    @GetMapping("/error/404")
    public String notFound(Model model) {
        model.addAttribute("message", "Страница не найдена.");
        return "error/404";
    }

    @GetMapping("/error/409")
    public String conflict(Model model) {
        model.addAttribute("message", "Конфликт данных.");
        return "error/409";
    }
}
