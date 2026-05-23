package ru.itis.aleksander.formach.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.HistoryService;

@Controller
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public String myHistory(@AuthenticationPrincipal SecurityUser principal,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        model.addAttribute("topics", historyService.myHistory(principal.getUser(), page, 20));
        model.addAttribute("tab", "viewed");
        return "history/list";
    }

    @GetMapping("/participated")
    public String myParticipated(@AuthenticationPrincipal SecurityUser principal,
                                 @RequestParam(defaultValue = "0") int page,
                                 Model model) {
        model.addAttribute("topics", historyService.myParticipated(principal.getUser(), page, 20));
        model.addAttribute("tab", "participated");
        return "history/list";
    }
}
