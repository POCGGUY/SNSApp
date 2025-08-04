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
import ru.pocgg.SNSApp.DTO.mappers.display.FriendshipDisplayMapper;
import ru.pocgg.SNSApp.services.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
@Tag(name = "Friendship", description = "Управление дружбой между пользователями")
public class FriendshipRestController extends TemplateController {

    private final FriendshipService friendshipService;
    private final FriendshipDisplayMapper friendshipDisplayMapper;

    @Operation(summary = "Получить список всех ваших дружб")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<FriendshipDisplayDTO>> listFriendships(@AuthenticationPrincipal(expression = "id")
                                                                      int userId) {
        List<FriendshipDisplayDTO> list = friendshipService.getUserFriendships(userId)
                .stream().map(friend -> friendshipDisplayMapper.toDTO(friend, userId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Удалить существующую дружбу")
    @PreAuthorize("hasRole('USER') and @friendshipPermissionService.canDeleteFriendship(principal.id, #friendId)")
    @DeleteMapping
    public ResponseEntity<Void> deleteFriendship(@AuthenticationPrincipal(expression = "id") int userId,
                                                 @RequestParam int friendId) {
        friendshipService.removeFriendship(userId, friendId);
        return ResponseEntity.noContent().build();
    }

}

