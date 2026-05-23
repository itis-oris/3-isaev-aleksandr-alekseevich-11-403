package ru.itis.aleksander.formach.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.itis.aleksander.formach.dto.request.CreatePostRequest;
import ru.itis.aleksander.formach.dto.request.CreateTopicRequest;
import ru.itis.aleksander.formach.dto.request.UpdateTopicRequest;
import ru.itis.aleksander.formach.dto.response.TopicResponse;
import ru.itis.aleksander.formach.exсeption.AccessDeniedException;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.BookmarkService;
import ru.itis.aleksander.formach.service.PostService;
import ru.itis.aleksander.formach.service.TagService;
import ru.itis.aleksander.formach.service.TopicService;

import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;
    private final TagService tagService;
    private final PostService postService;
    private final BookmarkService bookmarkService;

    @GetMapping
    public String list (
            @RequestParam(defaultValue = "") String search,
            @RequestParam(name = "tagIds", required = false) List<Long> tagIds,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "new") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<TopicResponse> topics = topicService.search(search, tagIds, status, sort, page, 20);
        model.addAttribute("topics", topics);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("tagIds", tagIds == null ? List.of() : tagIds);
        model.addAttribute("status", status);
        model.addAttribute("allTags",
                tagService.findAll().stream()
                        .map(t -> Map.of("id", t.getId(), "name", t.getName()))
                        .toList());
        return "topics/list";
    }

    @GetMapping("/create")
    public String createTopic (
            Model model
    ) {
        model.addAttribute("createRequest", new CreateTopicRequest());
        model.addAttribute("allTags", tagService.findAll());
        return "topics/create";
    }

    @PostMapping("/create")
    public String createTopic (
            @Valid @ModelAttribute("createRequest") CreateTopicRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allTags", tagService.findAll());
            model.addAttribute("createRequest", request);
            return "topics/create";
        }

        TopicResponse topic = topicService.create(request, securityUser.getUser());
        return "redirect:/topics/" + topic.getId();
    }

    @GetMapping("/{id}")
    public String viewTopic (
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model
    )
    {
        TopicResponse topic = topicService.getById(id, securityUser.getUser());
        model.addAttribute("topic", topic);
        model.addAttribute("currentUserId",  securityUser.getUser().getId());
        model.addAttribute("posts", postService.getByTopic(id, securityUser.getUser()));
        model.addAttribute("createPostRequest", new CreatePostRequest());
        model.addAttribute("isBookmarked",
                bookmarkService.isBookmarked(securityUser.getUser(), id));
        return "topics/view";
    }

    @GetMapping("/{id}/edit")
    public String editTopic (
        @PathVariable Long id,
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model
    ) {
        TopicResponse topic = topicService.getById(id, null);

        if (!securityUser.getUser().getId().equals(topic.getAuthorId())) {
            throw new AccessDeniedException("Только автор может редактировать тему");
        }
        UpdateTopicRequest request = new UpdateTopicRequest();
        request.setTitle(topic.getTitle());
        request.setDescription(topic.getDescription());
        request.setTagIds(topic.getTagIds());
        model.addAttribute("updateRequest", request);
        model.addAttribute("topicId", id);
        model.addAttribute("topic", topic);
        model.addAttribute("allTags", tagService.findAll());

        return "topics/edit";

    }

    @PostMapping("/{id}/edit")
    public String edit (
            @PathVariable Long id,
            @Valid @ModelAttribute("updateRequest") UpdateTopicRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("topicId", id);
            model.addAttribute("allTags", tagService.findAll());
            return "topics/edit";
        }

        topicService.update(id, request, securityUser.getUser());
        return "redirect:/topics/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        topicService.delete(id, securityUser.getUser());
        return "redirect:/topics";
    }

    @PostMapping("/{id}/close")
    public String close(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        topicService.close(id, securityUser.getUser());
        return "redirect:/topics/" + id;
    }

    @PostMapping("/{id}/open")
    public String open(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        topicService.open(id, securityUser.getUser());
        return "redirect:/topics/" + id;
    }
}