package ru.itis.aleksander.formach.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.itis.aleksander.formach.dto.response.UserResponse;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.SubscriptionService;
import ru.itis.aleksander.formach.service.TopicService;
import ru.itis.aleksander.formach.service.UserService;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final TopicService topicService;
    private final SubscriptionService subscriptionService;

    @GetMapping("/home")
    public String getUser(@AuthenticationPrincipal SecurityUser securityUser,
                          Model model) {
        User me = securityUser.getUser();
        UserResponse user = userService.toResponse(me);
        model.addAttribute("user", user);
        model.addAttribute("popularTopics",
                topicService.getAll(PageRequest.of(0, 5, Sort.by("viewCount").descending()))
                        .getContent());
        model.addAttribute("todayTopics",
                topicService.topicsCreatedAfter(
                        LocalDate.now().atStartOfDay(), 5));
        model.addAttribute("subscriptionFeed",
                topicService.recentFromAuthors(
                        subscriptionService.getFollowing(me), 5));
        return "home";
    }
}
