package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.DTO.display.FriendshipDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.FriendshipDisplayMapper;
import ru.pocgg.SNSApp.DTO.mappers.PostDisplayMapper;
import ru.pocgg.SNSApp.DTO.mappers.UserDisplayMapper;
import ru.pocgg.SNSApp.model.Friendship;
import ru.pocgg.SNSApp.model.Post;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.DTO.display.UserFacadeDisplayDTO;
import ru.pocgg.SNSApp.DTO.display.PostDisplayDTO;
import ru.pocgg.SNSApp.services.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users/facade")
@RequiredArgsConstructor
@Tag(name = "User Facade", description = "Информация о пользователе, его постах и друзьях")
public class UserFacadeRestController {

    private final UserService userService;
    private final PostService postService;
    private final FriendshipService friendshipService;
    private final PermissionCheckService permissionCheckService;
    private final UserDisplayMapper userDisplayMapper;
    private final PostDisplayMapper postDisplayMapper;
    private final FriendshipDisplayMapper friendshipDisplayMapper;

    @Operation(summary = "Получить полный профиль пользователя")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{userId}")
    public ResponseEntity<UserFacadeDisplayDTO> getUserFacade(@AuthenticationPrincipal(expression = "id")
                                                                  int currentUserId,
                                                              @PathVariable int userId) {
        checkCanViewProfile(currentUserId, userId);
        User user = userService.getUserById(userId);
        List<PostDisplayDTO> posts = new ArrayList<>();
        if(permissionCheckService.canViewPostsAtUser(currentUserId, userId)) {
            posts = getPostsDtosSortedByCreationDate(postService.getPostsByUserOwner(userId));
        }
        List<FriendshipDisplayDTO> friends =
                getFriendshipDtos(friendshipService.getUserFriendships(userId), userId);
        UserFacadeDisplayDTO dto = new UserFacadeDisplayDTO(userDisplayMapper.toDTO(user), posts, friends);
        return ResponseEntity.ok(dto);
    }

    private List<PostDisplayDTO> getPostsDtosSortedByCreationDate(List<Post> posts) {
        return posts.stream()
                .sorted(Comparator.comparing(Post::getCreationDate).reversed())
                .map(postDisplayMapper::toDTO)
                .collect(Collectors.toList());
    }

    private List<FriendshipDisplayDTO> getFriendshipDtos(List<Friendship> friends, int currentUserId) {
        return friends.stream().map(friend -> friendshipDisplayMapper.toDTO(friend, currentUserId))
                .collect(Collectors.toList());
    }

    private void checkCanViewProfile(int currentUserId, int targetUserId) {
        if (!permissionCheckService.canViewUserProfile(currentUserId, targetUserId)) {
            throw new AccessDeniedException("You are not authorized to view this profile");
        }
    }
}
