package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.DTO.create.CreatePostCommentDTO;
import ru.pocgg.SNSApp.DTO.display.PostCommentDisplayDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePostCommentDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.PostCommentDisplayMapper;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.services.*;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
@Tag(name = "Post Comment", description = "Управление комментариями к постам")
public class PostCommentRestController extends TemplateController {

    private final PostCommentService postCommentService;
    private final PostCommentDisplayMapper commentDisplayMapper;

    @Operation(summary = "Создать комментарий к посту")
    @PreAuthorize("hasRole('USER') and @postPermissionService.canCreateComment(principal.id, #postId)")
    @PostMapping
    public ResponseEntity<PostCommentDisplayDTO> createComment(@PathVariable int postId,
                                                               @AuthenticationPrincipal(expression = "id") int userId,
                                                               @Valid @RequestBody CreatePostCommentDTO dto) {
        PostComment comment = postCommentService.createComment(postId, userId, dto);
        PostCommentDisplayDTO body = commentDisplayMapper.toDTO(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Operation(summary = "Список комментариев поста")
    @PreAuthorize("hasRole('USER') and @postPermissionService.canViewComments(principal.id, #postId)")
    @GetMapping
    public ResponseEntity<List<PostCommentDisplayDTO>> listComments(@PathVariable int postId) {
        return ResponseEntity.ok(getDtosSortedByCreationDate(postCommentService.getCommentsByPostId(postId)));
    }

    @Operation(summary = "Получить комментарий по id")
    @PreAuthorize("hasRole('USER') and @postPermissionService.canViewComment(principal.id, #commentId)")
    @GetMapping("/{commentId}")
    public ResponseEntity<PostCommentDisplayDTO> getComment(@PathVariable int commentId) {
        PostComment comment = postCommentService.getCommentById(commentId);
        return ResponseEntity.ok(commentDisplayMapper.toDTO(comment));
    }

    @Operation(summary = "Редактировать комментарий")
    @PreAuthorize("hasRole('USER') and @postPermissionService.canModifyComment(principal.id, #commentId)")
    @PatchMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable int commentId,
                                              @Valid @RequestBody UpdatePostCommentDTO dto) {
        postCommentService.updateComment(commentId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить комментарий")
    @PreAuthorize("hasRole('USER') and @postPermissionService.canDeleteComment(principal.id, #commentId)")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable int commentId) {
        postCommentService.setDeleted(commentId, true);
        return ResponseEntity.noContent().build();
    }

    private List<PostCommentDisplayDTO> getDtosSortedByCreationDate(List<PostComment> comments) {
        return comments.stream()
                .sorted(Comparator.comparing(PostComment::getCreationDate).reversed())
                .map(commentDisplayMapper::toDTO)
                .collect(Collectors.toList());
    }
}

