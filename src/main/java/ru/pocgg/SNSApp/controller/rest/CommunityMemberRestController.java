package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.model.CommunityMember;
import ru.pocgg.SNSApp.model.CommunityMemberId;
import ru.pocgg.SNSApp.DTO.display.CommunityMemberDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.CommunityMemberDisplayMapper;
import ru.pocgg.SNSApp.model.CommunityRole;
import ru.pocgg.SNSApp.services.CommunityMemberService;

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

    @Operation(summary = "Стать участником сообщества")
    @PreAuthorize("hasRole('USER') and @communityPermissionService.canBecomeMember(principal.id, #communityId)")
    @PostMapping
    public ResponseEntity<CommunityMemberDisplayDTO> addMember(@AuthenticationPrincipal(expression = "id") int userId,
                                                               @PathVariable int communityId) {
        CommunityMember member = communityMemberService.createMember(communityId, userId, Instant.now());
        CommunityMemberDisplayDTO dto = communityMemberDisplayMapper.toDTO(member);
        return ResponseEntity.ok().body(dto);
    }

    @Operation(summary = "Список всех участников сообщества")
    @PreAuthorize("hasRole('USER') and @communityPermissionService.canViewMembers(principal.id, #communityId)")
    @GetMapping
    public ResponseEntity<List<CommunityMemberDisplayDTO>> listMembers(@PathVariable int communityId) {
        List<CommunityMemberDisplayDTO> list = getDTOsSortedByEntryDate(communityMemberService
                .getMembersByCommunityId(communityId));
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Получить конкретного участника")
    @PreAuthorize("hasRole('USER') and @communityPermissionService.canViewMembers(principal.id, #communityId)")
    @GetMapping("/{memberId}")
    public ResponseEntity<CommunityMemberDisplayDTO> getMember(@PathVariable int communityId,
                                                               @PathVariable int memberId) {
        CommunityMember member = communityMemberService.getMemberById(
                new CommunityMemberId(communityId, memberId));
        return ResponseEntity.status(HttpStatus.CREATED).body(communityMemberDisplayMapper.toDTO(member));
    }

    @Operation(summary = "Изменить роль у участника сообщества")
    @PreAuthorize("hasRole('USER') and @communityPermissionService.canChangeRole(principal.id, #communityId)")
    @GetMapping("/edit/role/{memberId}")
    public ResponseEntity<Void> setRole(@PathVariable int communityId,
                                        @PathVariable int memberId,
                                        @RequestParam CommunityRole role) {
        communityMemberService.setMemberRole(CommunityMemberId.builder()
                .communityId(communityId)
                .memberId(memberId).build(), role);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить участника из сообщества")
    @PreAuthorize("hasRole('USER') and @communityPermissionService" +
            ".canRemoveMember(principal.id, #memberId, #communityId)")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(@AuthenticationPrincipal(expression = "id") int userId,
                                             @PathVariable int communityId,
                                             @PathVariable int memberId) {
        communityMemberService.removeMember(
                new CommunityMemberId(communityId, memberId));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Перестать быть участником сообщества")
    @PreAuthorize("hasRole('USER') and @communityPermissionService.canLeaveCommunity(principal.id, #communityId)")
    @DeleteMapping("/me")
    public ResponseEntity<Void> leave(@AuthenticationPrincipal(expression = "id") int userId,
                                      @PathVariable int communityId) {
        communityMemberService.removeMember(
                new CommunityMemberId(communityId, userId));
        return ResponseEntity.noContent().build();
    }

    private List<CommunityMemberDisplayDTO> getDTOsSortedByEntryDate(List<CommunityMember> members) {
        return members.stream().sorted(Comparator.comparing(CommunityMember::getEntryDate).reversed())
                .map(communityMemberDisplayMapper::toDTO)
                .collect(Collectors.toList());
    }
}

