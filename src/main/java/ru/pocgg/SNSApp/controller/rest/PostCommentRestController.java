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
import ru.pocgg.SNSApp.DTO.mappers.PostCommentDisplayMapper;
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
    private final PermissionCheckService permissionCheckService;
    private final PostCommentDisplayMapper commentDisplayMapper;

    @Operation(summary = "Создать комментарий к посту")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<PostCommentDisplayDTO> createComment(@PathVariable int postId,
                                                               @AuthenticationPrincipal(expression = "id") int userId,
                                                               @Valid @RequestBody CreatePostCommentDTO dto) {
        checkCanViewPost(userId, postId);
        PostComment comment = postCommentService.createComment(postId, userId, dto);
        PostCommentDisplayDTO body = commentDisplayMapper.toDTO(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Operation(summary = "Список комментариев поста")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<PostCommentDisplayDTO>> listComments(@PathVariable int postId,
                                                                    @AuthenticationPrincipal(expression = "id")
                                                                    int userId) {
        checkCanViewPost(userId, postId);
        return ResponseEntity.ok(getDtosSortedByCreationDate(postCommentService.getCommentsByPostId(postId)));
    }

    @Operation(summary = "Получить комментарий по id")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{commentId}")
    public ResponseEntity<PostCommentDisplayDTO> getComment(@PathVariable int commentId,
                                                            @AuthenticationPrincipal(expression = "id") int userId) {
        int checkingPostId = postCommentService.getCommentById(commentId).getPost().getId();
        checkCanViewPost(userId, checkingPostId);
        PostComment comment = postCommentService.getCommentById(commentId);
        return ResponseEntity.ok(commentDisplayMapper.toDTO(comment));
    }

    @Operation(summary = "Редактировать комментарий")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable int commentId,
                                              @Valid @RequestBody UpdatePostCommentDTO dto,
                                              @AuthenticationPrincipal(expression = "id") int userId) {
        checkCanModifyComment(userId, commentId);
        postCommentService.updateComment(commentId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить комментарий")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable int commentId,
                                              @AuthenticationPrincipal(expression = "id") int userId) {
        checkCanDeleteComment(userId, commentId);
        postCommentService.setDeleted(commentId, true);
        return ResponseEntity.noContent().build();
    }

    private List<PostCommentDisplayDTO> getDtosSortedByCreationDate(List<PostComment> comments) {
        return comments.stream()
                .sorted(Comparator.comparing(PostComment::getCreationDate).reversed())
                .map(commentDisplayMapper::toDTO)
                .collect(Collectors.toList());
    }

    private void checkCanViewPost(int userId, int postId) {
        if (!permissionCheckService.canUserViewPost(userId, postId)) {
            throw new AccessDeniedException("You are not authorized to view this post");
        }
    }

    private void checkCanModifyComment(int userId, int commentId) {
        if (!permissionCheckService.canUserModifyPostComment(userId, commentId)) {
            throw new AccessDeniedException("You are not authorized to modify this comment");
        }
    }

    private void checkCanDeleteComment(int userId, int commentId) {
        if (!permissionCheckService.canUserDeletePostComment(userId, commentId)) {
            throw new AccessDeniedException("You are not authorized to delete this comment");
        }
    }
}

