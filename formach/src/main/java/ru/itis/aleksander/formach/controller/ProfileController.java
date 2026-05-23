package ru.itis.aleksander.formach.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.aleksander.formach.dto.request.UpdateUserRequest;
import ru.itis.aleksander.formach.dto.response.UserResponse;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.exсeption.AlreadyExistsException;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.PostService;
import ru.itis.aleksander.formach.service.SubscriptionService;
import ru.itis.aleksander.formach.service.TopicService;
import ru.itis.aleksander.formach.service.UserService;
import ru.itis.aleksander.formach.service.WallPostService;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final TopicService topicService;
    private final PostService postService;
    private final WallPostService wallPostService;

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        User me = userService.findById(securityUser.getUser().getId());
        UserResponse userResponse = userService.toResponseWithStats(me);
        model.addAttribute("user", userResponse);
        model.addAttribute("updateRequest", buildUpdateRequest(me));
        model.addAttribute("isOwn", true);
        model.addAttribute("followerCount", subscriptionService.countFollowers(me));
        model.addAttribute("followingCount", subscriptionService.countFollowing(me));
        return "profile";
    }

    @GetMapping("/users")
    public String usersList(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<UserResponse> users = userService.findAll(
                search, PageRequest.of(page, 20, Sort.by("login")));
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        return "users/list";
    }

    @GetMapping("/users/{id}/topics")
    public String userTopics(@PathVariable Long id,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {
        User user = userService.findById(id);
        model.addAttribute("targetUser", user);
        model.addAttribute("topics", topicService.findByAuthor(user, page, 20));
        return "users/topics";
    }

    @GetMapping("/users/{id}/posts")
    public String userPosts(@PathVariable Long id,
                            @RequestParam(defaultValue = "0") int page,
                            @AuthenticationPrincipal SecurityUser securityUser,
                            Model model) {
        User user = userService.findById(id);
        model.addAttribute("targetUser", user);
        model.addAttribute("posts",
                postService.getByAuthor(user, page, 20,
                        securityUser != null ? securityUser.getUser() : null));
        return "users/posts";
    }

    @GetMapping("/users/{id}")
    public String publicProfile(@PathVariable Long id,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                Model model) {
        User user = userService.findById(id);
        UserResponse userResponse = userService.toResponseWithStats(user);
        boolean isOwn = securityUser != null && securityUser.getUser().getId().equals(id);
        model.addAttribute("user", userResponse);
        model.addAttribute("isOwn", isOwn);
        model.addAttribute("followerCount", subscriptionService.countFollowers(user));
        model.addAttribute("followingCount", subscriptionService.countFollowing(user));
        model.addAttribute("wallPosts", wallPostService.wallOf(user, 0, 20));
        if (securityUser != null && !isOwn) {
            model.addAttribute("isFollowing",
                    subscriptionService.isFollowing(securityUser.getUser(), user));
        }
        return "users/view";
    }

    @PostMapping("/profile/edit")
    public String edit(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @ModelAttribute("updateRequest") UpdateUserRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam(value = "removeAvatar", required = false, defaultValue = "false") boolean removeAvatar,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            User fresh = userService.findById(securityUser.getUser().getId());
            model.addAttribute("user", userService.toResponseWithStats(fresh));
            model.addAttribute("isOwn", true);
            return "profile";
        }

        try {
            userService.update(securityUser.getUser().getId(), request, avatar, removeAvatar);
            refreshPrincipal(securityUser.getUser().getId(), securityUser);
        } catch (AlreadyExistsException e) {
            bindingResult.rejectValue("login", "error", e.getMessage());
            User fresh = userService.findById(securityUser.getUser().getId());
            model.addAttribute("user", userService.toResponseWithStats(fresh));
            model.addAttribute("isOwn", true);
            return "profile";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("avatar", e.getMessage());
            User fresh = userService.findById(securityUser.getUser().getId());
            model.addAttribute("user", userService.toResponseWithStats(fresh));
            model.addAttribute("isOwn", true);
            return "profile";
        }

        return "redirect:/profile";
    }

    private void refreshPrincipal(Long userId, SecurityUser oldPrincipal) {
        User fresh = userService.findById(userId);
        SecurityUser newPrincipal = new SecurityUser(fresh);
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(
                        newPrincipal,
                        oldPrincipal.getPassword(),
                        newPrincipal.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    private UpdateUserRequest buildUpdateRequest(User user) {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setLogin(user.getLogin());
        req.setFirstName(user.getFirstName());
        req.setLastName(user.getLastName());
        req.setGender(user.getGender());
        return req;
    }
}
