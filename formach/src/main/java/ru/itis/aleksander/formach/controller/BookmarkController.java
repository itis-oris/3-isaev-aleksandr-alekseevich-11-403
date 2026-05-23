package ru.itis.aleksander.formach.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.BookmarkService;

@Controller
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping
    public String myBookmarks(@AuthenticationPrincipal SecurityUser principal, Model model) {
        model.addAttribute("topics", bookmarkService.findAllOf(principal.getUser()));
        return "bookmarks/list";
    }

    @PostMapping("/topics/{topicId}/toggle")
    public String toggle(@PathVariable Long topicId,
                         @AuthenticationPrincipal SecurityUser principal) {
        bookmarkService.toggle(principal.getUser(), topicId);
        return "redirect:/topics/" + topicId;
    }
}
