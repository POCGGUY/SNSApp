package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.DTO.display.FriendshipRequestDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.FriendshipRequestDisplayMapper;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.services.*;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/friendshipRequests")
@RequiredArgsConstructor
@Tag(name = "Friendship Request", description = "Управление запросами в друзья")
public class FriendshipRequestRestController extends TemplateController {
    private final FriendshipRequestService friendshipRequestService;
    private final FriendshipRequestDisplayMapper friendshipRequestDisplayMapper;

    @Operation(summary = "Создать запрос в друзья")
    @PreAuthorize("hasRole('USER') and @friendshipPermissionService.canSendFriendRequest(principal.id, #receiverId)")
    @PostMapping
    public ResponseEntity<FriendshipRequestDisplayDTO> createRequest(@AuthenticationPrincipal(expression = "id")
                                                                         int userId,
                                                                     @RequestParam int receiverId) {
        FriendshipRequest request = friendshipRequestService.createRequest(userId, receiverId, Instant.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(friendshipRequestDisplayMapper.toDTO(request));
    }

    @Operation(summary = "Список отправленных запросов")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/sent")
    public ResponseEntity<List<FriendshipRequestDisplayDTO>> sent(@AuthenticationPrincipal(expression = "id")
                                                                      int userId) {
        return ResponseEntity
                .ok(getDTOsSortedByCreationDate(friendshipRequestService.getRequestsBySenderId(userId)));
    }

    @Operation(summary = "Список входящих запросов")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/received")
    public ResponseEntity<List<FriendshipRequestDisplayDTO>> received(@AuthenticationPrincipal(expression = "id")
                                                                          int userId) {
        return ResponseEntity
                .ok(getDTOsSortedByCreationDate(friendshipRequestService.getRequestsByReceiverId(userId)));
    }

    @Operation(summary = "Принять запрос в друзья")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/accept")
    public ResponseEntity<Void> acceptRequest(@RequestParam int senderId,
                                              @AuthenticationPrincipal(expression = "id") int userId) {
        friendshipRequestService.acceptRequest(
                FriendshipRequestId.builder()
                        .senderId(senderId)
                        .receiverId(userId)
                        .build());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Отклонить запрос в друзья")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/decline")
    public ResponseEntity<Void> declineRequest(@RequestParam int senderId,
                                              @AuthenticationPrincipal(expression = "id") int userId) {
        friendshipRequestService.declineRequest(FriendshipRequestId.builder()
                .senderId(senderId)
                .receiverId(userId)
                .build());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить запрос в друзья")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping
    public ResponseEntity<Void> deleteRequest(@RequestParam int receiverId,
                                              @AuthenticationPrincipal(expression = "id") int userId) {
        friendshipRequestService.deleteRequest(FriendshipRequestId.builder()
                .senderId(userId)
                .receiverId(receiverId)
                .build());
        return ResponseEntity.noContent().build();
    }

    private List<FriendshipRequestDisplayDTO> getDTOsSortedByCreationDate(List<FriendshipRequest> requests) {
        return requests.stream().sorted(Comparator.comparing(FriendshipRequest::getCreationDate).reversed())
                .map(friendshipRequestDisplayMapper::toDTO).collect(Collectors.toList());
    }
}
