package ru.itis.aleksander.formach.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.itis.aleksander.formach.service.BanAppealService;
import ru.itis.aleksander.formach.service.UserService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final BanAppealService banAppealService;
    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String login,
                            Model model) {
        if ("banned".equals(error) && login != null && !login.isBlank()) {
            banAppealService.findLatestByLogin(login)
                    .ifPresent(a -> model.addAttribute("appeal", a));
            model.addAttribute("banProofs", userService.getBanProofsByLogin(login));
        }
        return "login";
    }
}