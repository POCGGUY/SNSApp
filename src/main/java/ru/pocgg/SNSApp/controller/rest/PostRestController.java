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
import ru.pocgg.SNSApp.DTO.mappers.display.PostDisplayMapper;
import ru.pocgg.SNSApp.services.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "Управление постами")
public class PostRestController {

    private final PostService postService;
    private final UserService userService;
    private final CommunityService communityService;
    private final PostDisplayMapper postDisplayMapper;

    @Operation(summary = "Создать пост на странице пользователя")
    @PreAuthorize("hasRole('USER') and @postPermissionService.canCreateUserPost(principal.id, #ownerId)")
    @PostMapping("/users/{ownerId}")
    public ResponseEntity<PostDisplayDTO> createUserPost(@AuthenticationPrincipal(expression = "id") int authorId,
                                                         @PathVariable int ownerId,
                                                         @Valid @RequestBody CreatePostDTO dto) {
        userService.getUserById(ownerId);
        Post post = postService.createUserPost(ownerId, authorId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(postDisplayMapper.toDTO(post));
    }

    @Operation(summary = "Создать пост на странице сообщества")
    @PreAuthorize("hasRole('USER') and @postPermissionService.canCreateCommunityPost(principal.id, #ownerId)")
    @PostMapping("/communities/{ownerId}")
    public ResponseEntity<PostDisplayDTO> createCommunityPost(@AuthenticationPrincipal(expression = "id") int authorId,
                                                              @PathVariable int ownerId,
                                                              @Valid @RequestBody CreatePostDTO dto) {
        communityService.getCommunityById(ownerId);
        Post post = postService.createCommunityPost(ownerId, authorId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(postDisplayMapper.toDTO(post));
    }

    @Operation(summary = "Список постов у сообщества")
    @PreAuthorize("hasRole('USER') and @postPermissionService.canViewCommunityPosts(principal.id, #communityId)")
    @GetMapping("/communities/{communityId}")
    public ResponseEntity<List<PostDisplayDTO>> getPostsByCommunity(@AuthenticationPrincipal(expression = "id")
                                                                    int userId,
                                                                    @PathVariable int communityId) {
        List<PostDisplayDTO> dtos = getDTOsSortedByCreationDate(postService.getPostsByCommunityOwner(communityId));
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Список постов у пользователя")
    @PreAuthorize("hasRole('USER') and @postPermissionService.canViewUserPosts(principal.id, #targetUserId)")
    @GetMapping("/users/{targetUserId}")
    public ResponseEntity<List<PostDisplayDTO>> getPostsByUser(@AuthenticationPrincipal(expression = "id") int userId,
                                                               @PathVariable int targetUserId) {
        List<PostDisplayDTO> dtos = getDTOsSortedByCreationDate(postService.getPostsByUserOwner(targetUserId));
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Редактировать текст поста")
    @PreAuthorize("hasRole('USER') and @postPermissionService.canModifyPost(principal.id, #postId)")
    @PatchMapping("/{postId}")
    public ResponseEntity<Void> updatePost(@PathVariable int postId,
                                           @Valid @RequestBody UpdatePostDTO dto) {
        postService.updatePost(postId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить пост")
    @PreAuthorize("hasRole('USER') and @postPermissionService.canDeletePost(principal.id, #postId)")
    @DeleteMapping("/{postId}/delete")
    public ResponseEntity<Void> setDeleted(@PathVariable int postId) {
        postService.setIsDeleted(postId, true);
        return ResponseEntity.noContent().build();
    }

    private List<PostDisplayDTO> getDTOsSortedByCreationDate(List<Post> posts) {
        return posts.stream()
                .sorted(Comparator.comparing(Post::getCreationDate).reversed()).map(postDisplayMapper::toDTO).toList();
    }
}

