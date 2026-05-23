package ru.itis.aleksander.formach.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.aleksander.formach.dto.response.UserResponse;
import ru.itis.aleksander.formach.entity.Complaint;
import ru.itis.aleksander.formach.entity.ComplaintStatus;
import ru.itis.aleksander.formach.entity.Role;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.AdminService;
import ru.itis.aleksander.formach.service.BanAppealService;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final BanAppealService appealService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getDashboard());
        return "admin/index";
    }

    @GetMapping("/users")
    public String users(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<UserResponse> users = adminService.getUsers(
                search, PageRequest.of(page, 20, Sort.by("login")));
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        return "admin/users";
    }


    @PostMapping("/users/{id}/ban")
    public String ban(@PathVariable Long id,
                      @RequestParam(defaultValue = "") String reason,
                      @RequestParam(value = "bannedUntil", required = false)
                      @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
                      LocalDateTime bannedUntil,
                      @RequestParam(value = "proofs", required = false)
                      List<MultipartFile> proofs,
                      @RequestParam(defaultValue = "") String search,
                      @RequestParam(defaultValue = "0") int page,
                      @AuthenticationPrincipal SecurityUser principal) {

        adminService.banUser(
                principal.getUser(),
                id,
                reason,
                bannedUntil,
                proofs
        );

        return "redirect:/admin/users?search=" + search + "&page=" + page;
    }

    @PostMapping("/users/{id}/unban")
    public String unban(@PathVariable Long id,
                        @RequestParam(defaultValue = "") String search,
                        @RequestParam(defaultValue = "0") int page) {
        adminService.unbanUser(id);
        return "redirect:/admin/users?search=" + search + "&page=" + page;
    }

    @PostMapping("/users/{id}/role")
    public String setRole(@PathVariable Long id,
                          @RequestParam Role role,
                          @RequestParam(defaultValue = "") String search,
                          @RequestParam(defaultValue = "0") int page) {
        adminService.setRole(id, role);
        return "redirect:/admin/users?search=" + search + "&page=" + page;
    }

    @GetMapping("/complaints")
    public String complaints(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<Complaint> complaints = adminService.getComplaints(
                status, PageRequest.of(page, 20));
        model.addAttribute("complaints", complaints);
        model.addAttribute("status", status);
        return "admin/complaints";
    }

    @GetMapping("/complaints/{id}")
    public String complaintDetail(@PathVariable Long id, Model model) {
        model.addAttribute("c", adminService.getComplaint(id));
        return "admin/complaint-detail";
    }

    @PostMapping("/complaints/{id}/resolve")
    public String resolve(@PathVariable Long id,
                          @RequestParam(required = false) String resolution,
                          @RequestParam(defaultValue = "false") boolean deletePost,
                          @RequestParam(defaultValue = "false") boolean deleteTopic,
                          @RequestParam(defaultValue = "false") boolean banAuthor,
                          @RequestParam(required = false) String banReason,
                          @RequestParam(value = "bannedUntil", required = false)
                          @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime bannedUntil,
                          @AuthenticationPrincipal SecurityUser principal) {

        if (deletePost || deleteTopic || banAuthor) {
            adminService.resolveComplaintWithActions(principal.getUser(), id, resolution, deletePost, deleteTopic,
                    banAuthor, banReason, bannedUntil);
        } else {
            adminService.resolveComplaint(id, resolution);
        }
        return "redirect:/admin/complaints";
    }

    @PostMapping("/complaints/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String resolution) {
        adminService.rejectComplaint(id, resolution);
        return "redirect:/admin/complaints";
    }

    @PostMapping("/topics/{id}/delete")
    public String deleteTopic(@PathVariable Long id) {
        adminService.deleteTopic(id);
        return "redirect:/topics";
    }

    @PostMapping("/topics/{id}/pin")
    public String pinTopic(@PathVariable Long id,
                           @RequestParam(defaultValue = "true") boolean pinned) {
        adminService.setPinned(id, pinned);
        return "redirect:/topics/" + id;
    }

    @GetMapping("/appeals")
    public String appeals(@RequestParam(defaultValue = "") String status,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        model.addAttribute("appeals", appealService.list(status, page, 20));
        model.addAttribute("status", status);
        return "admin/appeals";
    }

    @PostMapping("/appeals/{id}/resolve")
    public String resolveAppeal(@PathVariable Long id,
                                @RequestParam(defaultValue = "") String status,
                                @RequestParam(required = false) String response) {
        appealService.setStatus(id, ComplaintStatus.REVIEWED, response);
        return "redirect:/admin/appeals?status=" + status;
    }

    @PostMapping("/appeals/{id}/reject")
    public String rejectAppeal(@PathVariable Long id,
                               @RequestParam(defaultValue = "") String status,
                               @RequestParam(required = false) String response) {
        appealService.setStatus(id, ComplaintStatus.REJECTED, response);
        return "redirect:/admin/appeals?status=" + status;
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id,
                             @RequestParam Long topicId) {
        adminService.deletePost(id);
        return "redirect:/topics/" + topicId;
    }
}
