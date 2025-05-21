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
import ru.pocgg.SNSApp.DTO.create.CreatePostDTO;
import ru.pocgg.SNSApp.DTO.display.PostDisplayDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePostDTO;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.DTO.mappers.PostDisplayMapper;
import ru.pocgg.SNSApp.services.*;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "Управление постами")
public class PostRestController {

    private final PostService postService;
    private final PermissionCheckService permissionCheckService;
    private final UserService userService;
    private final CommunityService communityService;
    private final PostDisplayMapper postDisplayMapper;

    @Operation(summary = "Создать пост на странице пользователя")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/users/{ownerId}")
    public ResponseEntity<PostDisplayDTO> createUserPost(@AuthenticationPrincipal(expression = "id") int authorId,
                                                         @PathVariable int ownerId,
                                                         @Valid @RequestBody CreatePostDTO dto) {
        checkCanCreateUserPost(authorId, ownerId);
        userService.getUserById(ownerId);
        Post post = postService.createUserPost(ownerId, authorId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(postDisplayMapper.toDTO(post));
    }

    @Operation(summary = "Создать пост на странице сообщества")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/communities/{ownerId}")
    public ResponseEntity<PostDisplayDTO> createCommunityPost(@AuthenticationPrincipal(expression = "id") int authorId,
                                                              @PathVariable int ownerId,
                                                              @Valid @RequestBody CreatePostDTO dto) {
        checkCanCreateCommunityPost(authorId, ownerId);
        communityService.getCommunityById(ownerId);
        Post post = postService.createCommunityPost(ownerId, authorId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(postDisplayMapper.toDTO(post));
    }

    @Operation(summary = "Список постов у сообщества")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/communities/{communityId}")
    public ResponseEntity<List<PostDisplayDTO>> getPostsByCommunity(@AuthenticationPrincipal(expression = "id")
                                                                    int userId,
                                                                    @PathVariable int communityId) {
        checkCanViewByCommunityOwner(userId, communityId);
        List<PostDisplayDTO> dtos = getDTOsSortedByCreationDate(postService.getPostsByCommunityOwner(communityId));
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Список постов у пользователя")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/users/{targetUserId}")
    public ResponseEntity<List<PostDisplayDTO>> getPostsByUser(@AuthenticationPrincipal(expression = "id") int userId,
                                                               @PathVariable int targetUserId) {
        checkCanViewByUserOwner(userId, targetUserId);
        List<PostDisplayDTO> dtos = getDTOsSortedByCreationDate(postService.getPostsByUserOwner(targetUserId));
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Редактировать текст поста")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{postId}")
    public ResponseEntity<Void> updatePost(@AuthenticationPrincipal(expression = "id") int userId,
                                           @PathVariable int postId,
                                           @Valid @RequestBody UpdatePostDTO dto) {
        checkCanModifyPost(userId, postId);
        postService.updatePostText(postId, dto.getText());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить пост")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{postId}/delete")
    public ResponseEntity<Void> setDeleted(@AuthenticationPrincipal(expression = "id") int userId,
                                           @PathVariable int postId) {
        checkCanDeletePost(userId, postId);
        postService.setIsDeleted(postId, true);
        return ResponseEntity.noContent().build();
    }

    private List<PostDisplayDTO> getDTOsSortedByCreationDate(List<Post> posts) {
        return posts.stream()
                .sorted(Comparator.comparing(Post::getCreationDate).reversed()).map(postDisplayMapper::toDTO).toList();
    }

    private void checkCanCreateUserPost(int authorId, int ownerId) {
        if (!permissionCheckService.canUserCreateUserPost(authorId, ownerId)) {
            throw new AccessDeniedException("you are not allowed to create post for this user");
        }
    }

    private void checkCanCreateCommunityPost(int authorId, int communityId) {
        if (!permissionCheckService.canUserCreateCommunityPost(authorId, communityId)) {
            throw new AccessDeniedException("you are not allowed to create post for this community");
        }
    }

    private void checkCanViewByUserOwner(int userId, int targetUserId) {
        if (!permissionCheckService.canViewPostsAtUser(userId, targetUserId)) {
            throw new AccessDeniedException("not allowed to view posts at this user");
        }
    }

    private void checkCanViewByCommunityOwner(int userId, int communityId) {
        if (!permissionCheckService.canViewPostsAtCommunity(userId, communityId)) {
            throw new AccessDeniedException("not allowed to view posts at this community");
        }
    }

    private void checkCanModifyPost(int userId, int postId) {
        if (!permissionCheckService.canUserModifyPost(userId, postId)) {
            throw new AccessDeniedException("not allowed to modify this post");
        }
    }

    private void checkCanDeletePost(int userId, int postId) {
        if (!permissionCheckService.canUserDeletePost(userId, postId)) {
            throw new AccessDeniedException("not allowed to delete this post");
        }
    }
}

