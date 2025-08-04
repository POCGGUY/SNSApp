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
import ru.pocgg.SNSApp.DTO.mappers.display.PostCommentDisplayMapper;
import ru.pocgg.SNSApp.DTO.mappers.display.PostDisplayMapper;
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
    private final PostDisplayMapper postDisplayMapper;
    private final PostCommentDisplayMapper commentDisplayMapper;
    private final PostCommentService postCommentService;

    @Operation(summary = "Пост с комментариями")
    @PreAuthorize("hasRole('USER') and @postPermissionService.canViewPost(principal.id, #postId)")
    @GetMapping("/{postId}")
    public ResponseEntity<PostFacadeDTO> getPostByCommunity(@PathVariable int postId) {
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

}
