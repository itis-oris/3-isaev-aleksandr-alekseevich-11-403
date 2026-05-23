package ru.itis.aleksander.formach.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.itis.aleksander.formach.dto.request.RegisterRequest;
import ru.itis.aleksander.formach.exсeption.AlreadyExistsException;
import ru.itis.aleksander.formach.service.EmailVerificationService;
import ru.itis.aleksander.formach.service.UserService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final UserService userService;

    private final EmailVerificationService emailVerificationService;

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.register(request);
        } catch (AlreadyExistsException e) {
            bindingResult.rejectValue(e.getFieldName(), "error", e.getMessage());
            return "register";
        }

        return "redirect:/auth/register/success";
    }

    @GetMapping("/register/success")
    public String registerSuccess() {
        return "register-success";
    }

    @GetMapping("/verify")
    public String verify(@RequestParam String token, Model model) {
        try {
            emailVerificationService.verify(token);
            model.addAttribute("message", "Email успешно подтверждён!");
            return "verify-success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "verify-fail";
        }
    }

}
