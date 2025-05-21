package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pocgg.SNSApp.DTO.display.PostCommentDisplayDTO;
import ru.pocgg.SNSApp.DTO.display.PostDisplayDTO;
import ru.pocgg.SNSApp.DTO.display.PostFacadeDTO;
import ru.pocgg.SNSApp.DTO.mappers.PostCommentDisplayMapper;
import ru.pocgg.SNSApp.DTO.mappers.PostDisplayMapper;
import ru.pocgg.SNSApp.model.PostComment;
import ru.pocgg.SNSApp.services.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts/facade")
@RequiredArgsConstructor
@Tag(name = "PostFacade", description = "Получить посты с комментариями")
public class PostFacadeRestController {
    private final PostService postService;
    private final PermissionCheckService permissionCheckService;
    private final UserService userService;
    private final CommunityService communityService;
    private final PostDisplayMapper postDisplayMapper;
    private final PostCommentDisplayMapper commentDisplayMapper;
    private final PostCommentService postCommentService;

    @Operation(summary = "Пост с комментариями")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{postId}")
    public ResponseEntity<PostFacadeDTO> getPostByCommunity(@AuthenticationPrincipal(expression = "id")
                                                                    int userId,
                                                                    @PathVariable int postId) {
        checkCanViewPost(userId, postId);
        PostDisplayDTO postDTO = postDisplayMapper.toDTO(postService.getPostById(postId));
        List<PostCommentDisplayDTO> postCommentsDTOs =
                getDtosSortedByCreationDate(postCommentService.getCommentsByPostId(postId));
        return ResponseEntity.ok(new PostFacadeDTO(postDTO, postCommentsDTOs));
    }

    private List<PostCommentDisplayDTO> getDtosSortedByCreationDate(List<PostComment> comments) {
        return comments.stream()
                .sorted(Comparator.comparing(PostComment::getCreationDate).reversed())
                .map(commentDisplayMapper::toDTO)
                .collect(Collectors.toList());
    }


    private void checkCanViewPost(int userId, int postId) {
        if (!permissionCheckService.canUserViewPost(userId, postId)) {
            throw new AccessDeniedException("you are not authorized to view this post");
        }
    }

}
