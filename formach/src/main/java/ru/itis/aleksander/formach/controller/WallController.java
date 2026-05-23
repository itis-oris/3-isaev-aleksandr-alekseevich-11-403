package ru.itis.aleksander.formach.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.UserService;
import ru.itis.aleksander.formach.service.WallPostService;

import java.util.List;

@Controller
@RequestMapping("/wall")
@RequiredArgsConstructor
public class WallController {

    private final WallPostService wallPostService;
    private final UserService userService;

    @PostMapping("/{ownerId}/publish")
    public String publish(@PathVariable Long ownerId,
                          @RequestParam(required = false) String content,
                          @RequestParam(value = "files", required = false) List<MultipartFile> files,
                          @AuthenticationPrincipal SecurityUser principal) {
        User owner = userService.findById(ownerId);
        wallPostService.publish(owner, principal.getUser(), content, files);
        return "redirect:/users/" + ownerId;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam Long ownerId,
                         @AuthenticationPrincipal SecurityUser principal) {
        wallPostService.delete(id, principal.getUser());
        return "redirect:/users/" + ownerId;
    }

    @PostMapping("/{id}/comment")
    public String comment(@PathVariable Long id,
                          @RequestParam String content,
                          @RequestParam Long ownerId,
                          @AuthenticationPrincipal SecurityUser principal) {
        wallPostService.comment(id, principal.getUser(), content);
        return "redirect:/users/" + ownerId;
    }

    @PostMapping("/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                                @AuthenticationPrincipal SecurityUser principal) {
        Long ownerId = wallPostService.deleteComment(commentId, principal.getUser());
        return "redirect:/users/" + ownerId;
    }
}
