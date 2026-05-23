package ru.itis.aleksander.formach.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.SubscriptionService;
import ru.itis.aleksander.formach.service.UserService;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    @PostMapping("/{id}/follow")
    public String follow(@PathVariable Long id,
                         @AuthenticationPrincipal SecurityUser securityUser) {
        subscriptionService.follow(securityUser.getUser(), id);
        return "redirect:/users/" + id;
    }

    @PostMapping("/{id}/unfollow")
    public String unfollow(@PathVariable Long id,
                           @AuthenticationPrincipal SecurityUser securityUser) {
        subscriptionService.unfollow(securityUser.getUser(), id);
        return "redirect:/users/" + id;
    }

    @GetMapping("/{id}/followers")
    public String followers(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("targetUser", user);
        model.addAttribute("users", subscriptionService.getFollowers(user));
        model.addAttribute("title", "Подписчики @" + user.getLogin());
        model.addAttribute("emptyText", "У этого пользователя пока нет подписчиков.");
        return "users/followlist";
    }

    @GetMapping("/{id}/following")
    public String following(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("targetUser", user);
        model.addAttribute("users", subscriptionService.getFollowing(user));
        model.addAttribute("title", "Подписки @" + user.getLogin());
        model.addAttribute("emptyText", "Пользователь ещё ни на кого не подписан.");
        return "users/followlist";
    }
}
