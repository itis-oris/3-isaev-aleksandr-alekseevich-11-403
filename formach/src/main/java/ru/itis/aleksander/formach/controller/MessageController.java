package ru.itis.aleksander.formach.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.itis.aleksander.formach.dto.request.SendMessageRequest;
import ru.itis.aleksander.formach.entity.Message;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.MessageService;
import ru.itis.aleksander.formach.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @GetMapping
    public String inbox(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        User me = securityUser.getUser();
        model.addAttribute("interlocutors", messageService.getInterlocutors(me));
        model.addAttribute("unread", messageService.countUnread(me));
        return "messages/inbox";
    }

    @GetMapping("/{userId}")
    public String conversation(
            @PathVariable Long userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model
    ) {
        User me = securityUser.getUser();
        messageService.markRead(me, userId);
        List<Message> messages = messageService.getConversation(me, userId);
        model.addAttribute("messages", messages);
        model.addAttribute("companion", userService.findById(userId));
        model.addAttribute("currentUserId", me.getId());
        model.addAttribute("sendRequest", new SendMessageRequest());
        return "messages/conversation";
    }

    @PostMapping("/{userId}")
    public String send(
            @PathVariable Long userId,
            @Valid @ModelAttribute("sendRequest") SendMessageRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            User me = securityUser.getUser();
            model.addAttribute("messages", messageService.getConversation(me, userId));
            model.addAttribute("companion", userService.findById(userId));
            model.addAttribute("currentUserId", me.getId());
            return "messages/conversation";
        }
        messageService.send(securityUser.getUser(), userId,
                request.getContent(), request.getAttachments());
        return "redirect:/messages/" + userId;
    }
}
