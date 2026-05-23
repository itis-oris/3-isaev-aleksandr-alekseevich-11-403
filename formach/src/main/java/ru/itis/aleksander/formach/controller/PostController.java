package ru.itis.aleksander.formach.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.itis.aleksander.formach.dto.request.CreatePostRequest;
import ru.itis.aleksander.formach.dto.request.UpdatePostRequest;
import ru.itis.aleksander.formach.dto.response.PostResponse;
import ru.itis.aleksander.formach.dto.response.TopicResponse;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.PostService;
import ru.itis.aleksander.formach.service.TopicService;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final TopicService topicService;

    @PostMapping("/topic/{topicId}")
    public String create(
            @PathVariable Long topicId,
            @Valid @ModelAttribute("createPostRequest") CreatePostRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            TopicResponse topic = topicService.getById(topicId, null);
            model.addAttribute("topic", topic);
            model.addAttribute("posts", postService.getByTopic(topicId, securityUser.getUser()));
            model.addAttribute("currentUserId", securityUser.getUser().getId());
            return "topics/view";
        }

        postService.create(topicId, request, securityUser.getUser());
        return "redirect:/topics/" + topicId;
    }

    @GetMapping("/{id}/edit")
    public String editPage(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model
    ) {
        PostResponse post = postService.toResponse(
                postService.findById(id), securityUser.getUser());
        UpdatePostRequest request = new UpdatePostRequest();
        request.setContent(post.getContent());
        model.addAttribute("updatePostRequest", request);
        model.addAttribute("post", post);
        return "post/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @Valid @ModelAttribute("updatePostRequest") UpdatePostRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("post", postService.toResponse(
                    postService.findById(id), securityUser.getUser()));
            return "post/edit";
        }

        PostResponse post = postService.update(id, request, securityUser.getUser());
        return "redirect:/topics/" + post.getTopicId();
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        PostResponse post = postService.toResponse(
                postService.findById(id), securityUser.getUser());
        postService.delete(id, securityUser.getUser());
        return "redirect:/topics/" + post.getTopicId();
    }

    @PostMapping("/{id}/pin")
    public String togglePin(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        PostResponse post = postService.toResponse(
                postService.findById(id), securityUser.getUser());
        postService.togglePin(id, securityUser.getUser());
        return "redirect:/topics/" + post.getTopicId();
    }

    @PostMapping("/{id}/like")
    public String toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        Long topicId = postService.findById(id).getTopic().getId();
        postService.toggleLike(id, securityUser.getUser());
        return "redirect:/topics/" + topicId;
    }

    @PostMapping("/{id}/best")
    public String setBestAnswer(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        PostResponse post = postService.toResponse(
                postService.findById(id), securityUser.getUser());
        postService.setBestAnswer(id, securityUser.getUser());
        return "redirect:/topics/" + post.getTopicId();
    }
}