package ru.itis.aleksander.formach.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.aleksander.formach.dto.request.CreateComplaintRequest;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.ComplaintService;

import java.util.List;

@Controller
@RequestMapping("/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @GetMapping("/new")
    public String form(@RequestParam(required = false) Long postId,
                       @RequestParam(required = false) Long topicId,
                       @RequestParam(required = false) Long userId,
                       Model model) {
        CreateComplaintRequest req = new CreateComplaintRequest();
        req.setPostId(postId);
        req.setTopicId(topicId);
        req.setReportedUserId(userId);
        model.addAttribute("complaintRequest", req);
        return "complaints/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("complaintRequest") CreateComplaintRequest request,
                         BindingResult bindingResult,
                         @RequestParam(value = "proofs", required = false) List<MultipartFile> proofs,
                         @AuthenticationPrincipal SecurityUser principal,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return "complaints/new";
        }
        complaintService.create(request, principal.getUser(), proofs);
        model.addAttribute("message", "Жалоба отправлена. Спасибо!");
        return "complaints/success";
    }
}
