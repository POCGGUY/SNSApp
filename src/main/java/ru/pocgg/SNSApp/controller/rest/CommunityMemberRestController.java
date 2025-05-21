package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.model.CommunityMember;
import ru.pocgg.SNSApp.model.CommunityMemberId;
import ru.pocgg.SNSApp.DTO.display.CommunityMemberDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.CommunityMemberDisplayMapper;
import ru.pocgg.SNSApp.model.CommunityRole;
import ru.pocgg.SNSApp.model.SystemRole;
import ru.pocgg.SNSApp.model.exceptions.BadRequestException;
import ru.pocgg.SNSApp.services.CommunityMemberService;
import ru.pocgg.SNSApp.services.PermissionCheckService;

import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/communities/{communityId}/members")
@RequiredArgsConstructor
@Tag(name = "Community Member", description = "Управление участниками сообщества")
public class CommunityMemberRestController extends TemplateController {
    private final CommunityMemberService communityMemberService;
    private final CommunityMemberDisplayMapper communityMemberDisplayMapper;
    private final PermissionCheckService permissionCheckService;

    @Operation(summary = "Стать участником сообщества")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<CommunityMemberDisplayDTO> addMember(@AuthenticationPrincipal(expression = "id") int userId,
                                                               @PathVariable int communityId) {
        checkCanViewCommunity(userId, communityId);
        checkIsNotMemberAlready(userId, communityId);
        CommunityMember member = communityMemberService.createMember(communityId, userId, Instant.now());
        CommunityMemberDisplayDTO dto = communityMemberDisplayMapper.toDTO(member);
        return ResponseEntity.ok().body(dto);
    }

    @Operation(summary = "Список всех участников сообщества")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<CommunityMemberDisplayDTO>> listMembers(@AuthenticationPrincipal(expression = "id")
                                                                       int userId,
                                                                       @PathVariable int communityId) {
        checkCanViewCommunity(userId, communityId);
        List<CommunityMemberDisplayDTO> list = getDTOsSortedByEntryDate(communityMemberService
                .getMembersByCommunityId(communityId));
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Получить конкретного участника")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{memberId}")
    public ResponseEntity<CommunityMemberDisplayDTO> getMember(@AuthenticationPrincipal(expression = "id") int userId,
                                                               @PathVariable int communityId,
                                                               @PathVariable int memberId) {
        checkCanViewCommunity(userId, communityId);
        CommunityMember member = communityMemberService.getMemberById(
                new CommunityMemberId(communityId, memberId));
        return ResponseEntity.status(HttpStatus.CREATED).body(communityMemberDisplayMapper.toDTO(member));
    }

    @Operation(summary = "Изменить роль у участника сообщества")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/edit/role/{memberId}")
    public ResponseEntity<Void> setRole(@AuthenticationPrincipal(expression = "id") int userId,
                                        @PathVariable int communityId,
                                        @PathVariable int memberId,
                                        @RequestParam CommunityRole role) {
        checkUserIsOwner(userId, communityId);
        communityMemberService.setMemberRole(CommunityMemberId.builder()
                .communityId(communityId)
                .memberId(memberId).build(), role);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить участника из сообщества")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(@AuthenticationPrincipal(expression = "id") int userId,
                                             @PathVariable int communityId,
                                             @PathVariable int memberId) {
        checkCanEditCommunity(userId, communityId);
        checkTargetIsOwnerOrNotOnDelete(userId, communityId);
        communityMemberService.removeMember(
                new CommunityMemberId(communityId, memberId));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Перестать быть участником сообщества")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/me")
    public ResponseEntity<Void> leave(@AuthenticationPrincipal(expression = "id") int userId,
                                      @PathVariable int communityId) {
        checkCanLeaveCommunity(userId, communityId);
        communityMemberService.removeMember(
                new CommunityMemberId(communityId, userId));
        return ResponseEntity.noContent().build();
    }

    private List<CommunityMemberDisplayDTO> getDTOsSortedByEntryDate(List<CommunityMember> members) {
        return members.stream().sorted(Comparator.comparing(CommunityMember::getEntryDate).reversed())
                .map(communityMemberDisplayMapper::toDTO)
                .collect(Collectors.toList());
    }

    private void checkIsNotMemberAlready(int userId, int communityId) {
        if (permissionCheckService.isUserCommunityMember(userId, communityId)) {
            throw new BadRequestException("you are already a member of this community");
        }
    }

    private void checkCanViewCommunity(int userId, int communityId) {
        if (!permissionCheckService.canUserViewCommunity(userId, communityId)) {
            throw new AccessDeniedException("You are not authorized to view this community");
        }
    }

    private void checkCanEditCommunity(int userId, int communityId) {
        if (!permissionCheckService.canUserEditCommunity(userId, communityId)) {
            throw new AccessDeniedException("You are not authorized to delete this member from this community");
        }
    }

    private void checkTargetIsOwnerOrNotOnDelete(int userId, int communityId) {
        if (permissionCheckService.isUserCommunityOwner(userId, communityId)) {
            throw new AccessDeniedException("Owner cant leave or be kicked out of this community");
        }
    }

    private void checkUserIsOwner(int userId, int communityId) {
        if (!permissionCheckService.isUserCommunityOwner(userId, communityId)) {
            throw new AccessDeniedException("Only owner can set roles to users");
        }
    }

    private void checkCanLeaveCommunity(int userId, int communityId) {
        checkTargetIsOwnerOrNotOnDelete(userId, communityId);
        if (!permissionCheckService.isUserCommunityMember(userId, communityId)) {
            throw new AccessDeniedException("You are not a member of this community");
        }
    }
}

