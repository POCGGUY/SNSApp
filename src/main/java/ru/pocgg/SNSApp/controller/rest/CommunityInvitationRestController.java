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
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.DTO.display.CommunityInvitationDisplayDTO;
import ru.pocgg.SNSApp.DTO.create.CreateCommunityInvitationDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.CommunityInvitationDisplayMapper;
import ru.pocgg.SNSApp.model.exceptions.BadRequestException;
import ru.pocgg.SNSApp.services.*;

import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/communityInvitations")
@RequiredArgsConstructor
@Tag(name = "Community Invitation", description = "Приглашения в сообщества")
public class CommunityInvitationRestController {
    private final CommunityInvitationService communityInvitationService;
    private final UserService userService;
    private final CommunityService communityService;
    private final CommunityInvitationDisplayMapper communityInvitationDisplayMapper;

    @Operation(summary = "Создать приглашение в сообщество")
    @PreAuthorize("hasRole('USER') " +
            "and @communityPermissionService.canInviteToCommunity(principal.id, #receiverId, #communityId)")
    @PostMapping
    public ResponseEntity<CommunityInvitationDisplayDTO> create(@AuthenticationPrincipal(expression = "id") int userId,
                                                      @RequestParam int communityId,
                                                      @RequestParam int receiverId,
                                                      @Valid @RequestBody CreateCommunityInvitationDTO dto) {
        userService.getUserById(receiverId);
        communityService.getCommunityById(communityId);
        CommunityInvitation invitation = communityInvitationService.createInvitation(
                userId, receiverId, communityId, Instant.now(), dto);
        CommunityInvitationDisplayDTO DTO = communityInvitationDisplayMapper.toDTO(invitation);
        return ResponseEntity.status(HttpStatus.CREATED).body(DTO);
    }

    @Operation(summary = "Список всех приглашений сообщества")
    @PreAuthorize("hasRole('USER') and @communityPermissionService.canViewInvitations(principal.id, #communityId)")
    @GetMapping("/{communityId}")
    public ResponseEntity<List<CommunityInvitationDisplayDTO>> byCommunity(@PathVariable int communityId) {
        List<CommunityInvitationDisplayDTO> DTOs =
                getDTOsSortedByCreationDate(communityInvitationService.getInvitationsByCommunityId(communityId));
        return ResponseEntity.ok(DTOs);
    }

    @Operation(summary = "Список отправленных приглашений")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/sent")
    public ResponseEntity<List<CommunityInvitationDisplayDTO>> sent(@AuthenticationPrincipal(expression = "id")
                                                                        int userId) {
        List<CommunityInvitationDisplayDTO> DTOs =
                getDTOsSortedByCreationDate(communityInvitationService.getInvitationsBySenderId(userId));
        return ResponseEntity.ok(DTOs);
    }

    @Operation(summary = "Список входящих приглашений")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/received")
    public ResponseEntity<List<CommunityInvitationDisplayDTO>> received(@AuthenticationPrincipal(expression = "id")
                                                                            int userId) {
        List<CommunityInvitationDisplayDTO> DTOs =
                getDTOsSortedByCreationDate(communityInvitationService.getInvitationsByReceiverId(userId));
        return ResponseEntity.ok(DTOs);
    }

    @Operation(summary = "Удалить своё приглашение")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{communityId}/{receiverId}")
    public ResponseEntity<Void> deleteOwn(@PathVariable int communityId,
                                          @PathVariable int receiverId,
                                          @AuthenticationPrincipal(expression = "id") int userId) {
        communityInvitationService.removeInvitation(new CommunityInvitationId(userId, receiverId, communityId));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить любое приглашение")
    @PreAuthorize("hasRole('USER') and @communityPermissionService.canDeleteInvitation(principal.id, " +
            "#senderId, " +
            "#receiverId, " +
            "#communityId)")
    @DeleteMapping("/{communityId}/{senderId}/{receiverId}")
    public ResponseEntity<Void> deleteAny(@PathVariable int communityId,
                                          @PathVariable int senderId,
                                          @PathVariable int receiverId,
                                          @AuthenticationPrincipal(expression = "id") int userId) {
        communityInvitationService.removeInvitation(new CommunityInvitationId(senderId, receiverId, communityId));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Принять приглашение")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{communityId}/{senderId}/accept")
    public ResponseEntity<Void> accept(@PathVariable int communityId,
                                       @PathVariable int senderId,
                                       @AuthenticationPrincipal(expression = "id") int userId) {
        communityInvitationService.acceptInvitation(new CommunityInvitationId(senderId, userId, communityId));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Отклонить приглашение")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{communityId}/{senderId}/decline")
    public ResponseEntity<Void> decline(@PathVariable int communityId,
                                        @PathVariable int senderId,
                                        @AuthenticationPrincipal(expression = "id") int userId) {
        communityInvitationService.declineInvitation(new CommunityInvitationId(senderId, userId, communityId));
        return ResponseEntity.noContent().build();
    }

    private List<CommunityInvitationDisplayDTO> getDTOsSortedByCreationDate(List<CommunityInvitation> invitations) {
        return invitations.stream().sorted(Comparator.comparing(CommunityInvitation::getCreationDate).reversed())
                .map(communityInvitationDisplayMapper::toDTO).collect(Collectors.toList());
    }
}

