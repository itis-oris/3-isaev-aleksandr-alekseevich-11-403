package ru.itis.aleksander.formach.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.itis.aleksander.formach.dto.request.CreatePostRequest;
import ru.itis.aleksander.formach.dto.request.UpdatePostRequest;
import ru.itis.aleksander.formach.dto.response.PostResponse;
import ru.itis.aleksander.formach.entity.Post;
import ru.itis.aleksander.formach.repository.LikeRepository;
import ru.itis.aleksander.formach.security.model.SecurityUser;
import ru.itis.aleksander.formach.service.PostService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts API", description = "Управление постами и лайками")
public class PostRestController {

    private final PostService postService;
    private final LikeRepository likeRepository;

    @Operation(summary = "Список постов темы (верхний уровень)")
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<PostResponse>> getByTopic(
            @Parameter(description = "ID темы") @PathVariable Long topicId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        return ResponseEntity.ok(postService.getByTopic(topicId, securityUser.getUser()));
    }

    @Operation(summary = "Получить пост по ID")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        return ResponseEntity.ok(
                postService.toResponse(postService.findById(id), securityUser.getUser()));
    }

    @Operation(
        summary = "Создать пост",
        responses = {
            @ApiResponse(responseCode = "200", description = "Пост создан",
                content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
        }
    )
    @PostMapping("/topic/{topicId}")
    public ResponseEntity<PostResponse> create(
            @PathVariable Long topicId,
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        return ResponseEntity.ok(postService.create(topicId, request, securityUser.getUser()));
    }

    @Operation(summary = "Обновить пост")
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        return ResponseEntity.ok(postService.update(id, request, securityUser.getUser()));
    }

    @Operation(summary = "Удалить пост")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        postService.delete(id, securityUser.getUser());
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Поставить / убрать лайк",
        responses = @ApiResponse(responseCode = "200",
            description = "likeCount — новое количество, liked — поставлен ли лайк")
    )
    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        postService.toggleLike(id, securityUser.getUser());

        Post post = postService.findById(id);
        long count = likeRepository.countByPost(post);
        boolean liked = likeRepository.existsByPostAndUser(post, securityUser.getUser());

        return ResponseEntity.ok(Map.of("likeCount", count, "liked", liked));
    }
}
