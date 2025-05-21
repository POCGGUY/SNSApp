package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.display.CommunityMemberDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.CommunityMemberDisplayMapper;
import ru.pocgg.SNSApp.controller.rest.CommunityMemberRestController;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.model.exceptions.BadRequestException;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.CommunityMemberService;
import ru.pocgg.SNSApp.services.PermissionCheckService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityMemberRestControllerTest {

    @Mock
    private CommunityMemberService communityMemberService;

    @Mock
    private CommunityMemberDisplayMapper communityMemberDisplayMapper;

    @Mock
    private PermissionCheckService permissionCheckService;

    @InjectMocks
    private CommunityMemberRestController controller;

    private int userId;
    private int communityId;
    private int memberId;
    private Community community;
    private Instant entryDate;
    private CommunityMember communityMember;
    private CommunityMemberDisplayDTO memberDto;
    private List<CommunityMember> memberList;
    private List<CommunityMemberDisplayDTO> dtoList;

    @BeforeEach
    void setUp() {
        userId = 1;
        communityId = 2;
        memberId = 3;
        entryDate = Instant.now();

        User ownerUser = User.builder().userName("r").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("r@e").firstName("R").secondName("R")
                .gender(null).systemRole(null)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        ownerUser.setId(userId);

        community = Community.builder()
                .owner(ownerUser)
                .communityName("Test")
                .creationDate(Instant.now())
                .description("desc")
                .isPrivate(false)
                .deleted(false)
                .banned(false)
                .build();
        community.setId(communityId);

        communityMember = CommunityMember.builder()
                .community(community)
                .member(ownerUser)
                .memberRole(CommunityRole.MEMBER)
                .entryDate(entryDate)
                .build();

        memberDto = CommunityMemberDisplayDTO.builder()
                .communityId(communityMember.getCommunity().getId())
                .memberId(communityMember.getMember().getId())
                .memberRole(communityMember.getMemberRole().toString())
                .entryDate(communityMember.getEntryDate().toString())
                .build();

        memberList = Arrays.asList(communityMember);
        dtoList = Arrays.asList(memberDto);
    }

    @Test
    void addMember_positive() {
        when(permissionCheckService.canUserViewCommunity(userId, communityId)).thenReturn(true);

        when(permissionCheckService.isUserCommunityMember(userId, communityId)).thenReturn(false);

        when(communityMemberService.createMember(eq(communityId), eq(userId), any()))
                .thenReturn(communityMember);

        when(communityMemberDisplayMapper.toDTO(communityMember)).thenReturn(memberDto);

        ResponseEntity<CommunityMemberDisplayDTO> resp =
                controller.addMember(userId, communityId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(memberDto, resp.getBody());
    }

    @Test
    void addMember_negative() {
        when(permissionCheckService.canUserViewCommunity(userId, communityId)).thenReturn(true);

        when(permissionCheckService.isUserCommunityMember(userId, communityId)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> controller.addMember(userId, communityId));
    }

    @Test
    void listMembers_positive() {
        when(permissionCheckService.canUserViewCommunity(userId, communityId)).thenReturn(true);

        when(communityMemberService.getMembersByCommunityId(communityId)).thenReturn(memberList);

        when(communityMemberDisplayMapper.toDTO(communityMember)).thenReturn(memberDto);

        ResponseEntity<List<CommunityMemberDisplayDTO>> resp =
                controller.listMembers(userId, communityId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void listMembers_negative() {
        when(permissionCheckService.canUserViewCommunity(userId, communityId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.listMembers(userId, communityId));
    }

    @Test
    void getMember_positive() {
        when(permissionCheckService.canUserViewCommunity(userId, communityId)).thenReturn(true);

        when(communityMemberService.getMemberById(
                new CommunityMemberId(communityId, memberId))).thenReturn(communityMember);

        when(communityMemberDisplayMapper.toDTO(communityMember)).thenReturn(memberDto);

        ResponseEntity<CommunityMemberDisplayDTO> resp =
                controller.getMember(userId, communityId, memberId);

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(memberDto, resp.getBody());
    }

    @Test
    void getMember_negative() {
        when(permissionCheckService.canUserViewCommunity(userId, communityId)).thenReturn(true);

        when(communityMemberService.getMemberById(
                new CommunityMemberId(communityId, memberId)))
                .thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class,
                () -> controller.getMember(userId, communityId, memberId));
    }

    @Test
    void setRole_positive() {
        when(permissionCheckService.isUserCommunityOwner(userId, communityId)).thenReturn(true);

        doNothing().when(communityMemberService).setMemberRole(
                CommunityMemberId.builder()
                        .communityId(communityId)
                        .memberId(memberId)
                        .build(),
                CommunityRole.MODERATOR);

        ResponseEntity<Void> resp =
                controller.setRole(userId, communityId, memberId, CommunityRole.MODERATOR);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void setRole_negative() {
        when(permissionCheckService.isUserCommunityOwner(userId, communityId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.setRole(userId, communityId, memberId, CommunityRole.MODERATOR));
    }

    @Test
    void removeMember_positive() {
        when(permissionCheckService.canUserEditCommunity(userId, communityId)).thenReturn(true);

        when(permissionCheckService.isUserCommunityOwner(userId, communityId)).thenReturn(false);

        doNothing().when(communityMemberService).removeMember(
                new CommunityMemberId(communityId, memberId));

        ResponseEntity<Void> resp =
                controller.removeMember(userId, communityId, memberId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void removeMember_negative() {
        when(permissionCheckService.canUserEditCommunity(userId, communityId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.removeMember(userId, communityId, memberId));
    }

    @Test
    void leave_positive() {
        when(permissionCheckService.isUserCommunityOwner(userId, communityId)).thenReturn(false);

        when(permissionCheckService.isUserCommunityMember(userId, communityId)).thenReturn(true);

        doNothing().when(communityMemberService).removeMember(
                new CommunityMemberId(communityId, userId));

        ResponseEntity<Void> resp =
                controller.leave(userId, communityId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void leave_negative() {
        when(permissionCheckService.isUserCommunityOwner(userId, communityId)).thenReturn(false);

        when(permissionCheckService.isUserCommunityMember(userId, communityId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.leave(userId, communityId));
    }
}
