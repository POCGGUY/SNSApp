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
import ru.pocgg.SNSApp.DTO.mappers.CommunityInvitationDisplayMapper;
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
    private final PermissionCheckService permissionCheckService;
    private final UserService userService;
    private final CommunityService communityService;
    private final CommunityInvitationDisplayMapper communityInvitationDisplayMapper;
    private final CommunityMemberService communityMemberService;

    @Operation(summary = "Создать приглашение в сообщество")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<CommunityInvitationDisplayDTO> create(@AuthenticationPrincipal(expression = "id") int userId,
                                                      @RequestParam int communityId,
                                                      @RequestParam int receiverId,
                                                      @Valid @RequestBody CreateCommunityInvitationDTO dto) {
        checkIsReceiverOrCommunityActive(receiverId, communityId);
        checkCanInvite(userId, communityId);
        checkIsUserAlreadyACommunityMember(communityId, receiverId);
        checkIsInvitationExist(userId, receiverId, communityId);
        checkIsUserAlreadyInvitedInCommunity(receiverId, communityId);
        checkIsCommunityPublic(communityId);
        userService.getUserById(receiverId);
        communityService.getCommunityById(communityId);
        CommunityInvitation invitation = communityInvitationService.createInvitation(
                userId, receiverId, communityId, Instant.now(), dto);
        CommunityInvitationDisplayDTO DTO = communityInvitationDisplayMapper.toDTO(invitation);
        return ResponseEntity.status(HttpStatus.CREATED).body(DTO);
    }

    @Operation(summary = "Список всех приглашений сообщества")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{communityId}")
    public ResponseEntity<List<CommunityInvitationDisplayDTO>> byCommunity(@AuthenticationPrincipal(expression = "id") int userId,
                                                                 @PathVariable int communityId) {
        checkCanViewInvitations(userId, communityId);
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
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{communityId}/{senderId}/{receiverId}")
    public ResponseEntity<Void> deleteAny(@PathVariable int communityId,
                                          @PathVariable int senderId,
                                          @PathVariable int receiverId,
                                          @AuthenticationPrincipal(expression = "id") int userId) {
        checkCanDelete(userId, senderId, receiverId, communityId);
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

    private void checkIsCommunityPublic(int communityId) {
        Community community = communityService.getCommunityById(communityId);
        if(!community.getIsPrivate()){
            throw new BadRequestException("you cant send invitation to the public community");
        }
    }

    private void checkIsInvitationExist(int senderId, int receiverId, int communityId) {
        CommunityInvitationId id = CommunityInvitationId.builder().senderId(senderId)
                .receiverId(receiverId)
                .communityId(communityId)
                .build();
        if(communityInvitationService.isInvitationExist(id)){
            throw new BadRequestException("invitation with to community with id: " + id.getCommunityId()
                    + " by a sender with id: " + id.getSenderId() + " to receiver with id: " + id.getReceiverId() +
                    " already exist");
        }
    }

    private List<CommunityInvitationDisplayDTO> getDTOsSortedByCreationDate(List<CommunityInvitation> invitations) {
        return invitations.stream().sorted(Comparator.comparing(CommunityInvitation::getCreationDate).reversed())
                .map(communityInvitationDisplayMapper::toDTO).collect(Collectors.toList());
    }

    private void checkIsUserAlreadyInvitedInCommunity(int receiverId, int communityId) {
        if(communityInvitationService.isUserAlreadyInvited(receiverId, communityId)){
            throw new BadRequestException("This user is already invited in this community");
        }
    }

    private void checkIsUserAlreadyACommunityMember(int communityId, int receiverId) {
        if(communityMemberService.isMemberExist(new CommunityMemberId(communityId, receiverId))){
            throw new BadRequestException("This user is already a community member");
        }
    }

    private void checkCanInvite(int userId, int communityId) {
        if (!permissionCheckService.isUserCommunityModerator(userId, communityId)) {
            throw new AccessDeniedException("You are not authorized to invite in this community");
        }
    }

    private void checkIsReceiverOrCommunityActive(int receiverId, int communityId) {
        if(!permissionCheckService.isUserActive(receiverId)) {
            throw new BadRequestException("cant sent invitation to deactivated user");
        }
        if(!permissionCheckService.isCommunityActive(communityId)) {
            throw new BadRequestException("cant sent invitation into deactivated community");
        }
    }

    private void checkCanViewInvitations(int userId, int communityId) {
        if (!permissionCheckService.canViewInvitationsInCommunity(userId, communityId)) {
            throw new AccessDeniedException("You are not authorized to view invitations in this community");
        }
    }

    private void checkCanDelete(int userId, int senderId, int receiverId, int communityId) {
        CommunityInvitationId id = CommunityInvitationId.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .communityId(communityId)
                .build();
        if (!permissionCheckService.canUserDeleteCommunityInvitation(userId, id)) {
            throw new AccessDeniedException("Only sender or community or system moderator can delete this invitation");
        }
    }
}

