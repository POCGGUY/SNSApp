package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.create.CreateCommunityInvitationDTO;
import ru.pocgg.SNSApp.DTO.display.CommunityInvitationDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.CommunityInvitationDisplayMapper;
import ru.pocgg.SNSApp.controller.rest.CommunityInvitationRestController;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.model.exceptions.BadRequestException;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityInvitationRestControllerTest {

    @Mock
    private CommunityInvitationService communityInvitationService;

    @Mock
    private PermissionCheckService permissionCheckService;

    @Mock
    private UserService userService;

    @Mock
    private CommunityService communityService;

    @Mock
    private CommunityInvitationDisplayMapper communityInvitationDisplayMapper;

    @Mock
    private CommunityMemberService communityMemberService;

    @InjectMocks
    private CommunityInvitationRestController controller;

    private int senderId;
    private int receiverId;
    private int communityId;
    private Instant creationDate;
    private CreateCommunityInvitationDTO createDto;
    private CommunityInvitation invitation;
    private CommunityInvitationDisplayDTO invitationDto;
    private List<CommunityInvitation> invitationList;
    private List<CommunityInvitationDisplayDTO> dtoList;

    @BeforeEach
    void setUp() {
        senderId = 1;
        receiverId = 2;
        communityId = 3;
        creationDate = Instant.now();

        createDto = CreateCommunityInvitationDTO.builder()
                .description("test")
                .build();

        User sender = User.builder().userName("r").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("r@e").firstName("R").secondName("R")
                .gender(null).systemRole(null)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        sender.setId(senderId);

        User receiver = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        receiver.setId(receiverId);

        Community community = Community.builder()
                .owner(sender)
                .communityName("Test")
                .creationDate(Instant.now())
                .description("desc")
                .isPrivate(true)
                .deleted(false)
                .banned(false)
                .build();

        invitation = CommunityInvitation.builder()
                .sender(sender)
                .receiver(receiver)
                .community(community)
                .creationDate(creationDate)
                .build();

        invitationDto = CommunityInvitationDisplayDTO.builder()
                .senderId(invitation.getSender().getId())
                .receiverId(invitation.getReceiver().getId())
                .communityId(invitation.getCommunity().getId())
                .creationDate(invitation.getCreationDate().toString())
                .description(invitation.getDescription())
                .build();

        invitationList = Arrays.asList(invitation);
        dtoList = Arrays.asList(invitationDto);
    }

    @Test
    void create_positive() {
        when(permissionCheckService.isUserActive(receiverId)).thenReturn(true);

        when(permissionCheckService.isCommunityActive(communityId)).thenReturn(true);

        when(permissionCheckService.isUserCommunityModerator(senderId, communityId)).thenReturn(true);

        when(communityMemberService.isMemberExist(
                new CommunityMemberId(communityId, receiverId))).thenReturn(false);

        when(communityInvitationService.isInvitationExist(
                CommunityInvitationId.builder()
                        .senderId(senderId)
                        .receiverId(receiverId)
                        .communityId(communityId).build()))
                .thenReturn(false);

        when(communityInvitationService.isUserAlreadyInvited(receiverId, communityId))
                .thenReturn(false);

        when(communityService.getCommunityById(communityId))
                .thenReturn(invitation.getCommunity());

        when(userService.getUserById(receiverId))
                .thenReturn(invitation.getReceiver());

        when(communityInvitationService.createInvitation(eq(senderId), eq(receiverId), eq(communityId), any(), eq(createDto)))
                .thenReturn(invitation);

        when(communityInvitationDisplayMapper.toDTO(invitation)).thenReturn(invitationDto);

        ResponseEntity<CommunityInvitationDisplayDTO> resp =
                controller.create(senderId, communityId, receiverId, createDto);

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(invitationDto, resp.getBody());
    }

    @Test
    void create_negative() {
        when(permissionCheckService.isUserActive(receiverId)).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> controller.create(senderId, communityId, receiverId, createDto));
    }

    @Test
    void byCommunity_positive() {
        when(permissionCheckService.canViewInvitationsInCommunity(senderId, communityId)).thenReturn(true);

        when(communityInvitationService.getInvitationsByCommunityId(communityId))
                .thenReturn(invitationList);

        when(communityInvitationDisplayMapper.toDTO(invitation)).thenReturn(invitationDto);

        ResponseEntity<List<CommunityInvitationDisplayDTO>> resp =
                controller.byCommunity(senderId, communityId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void byCommunity_negative() {
        when(permissionCheckService.canViewInvitationsInCommunity(senderId, communityId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.byCommunity(senderId, communityId));
    }

    @Test
    void sent_positive() {
        when(communityInvitationService.getInvitationsBySenderId(senderId))
                .thenReturn(invitationList);

        when(communityInvitationDisplayMapper.toDTO(invitation)).thenReturn(invitationDto);

        ResponseEntity<List<CommunityInvitationDisplayDTO>> resp =
                controller.sent(senderId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void sent_negative() {
        when(communityInvitationService.getInvitationsBySenderId(senderId))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> controller.sent(senderId));
    }

    @Test
    void received_positive() {
        when(communityInvitationService.getInvitationsByReceiverId(senderId))
                .thenReturn(invitationList);

        when(communityInvitationDisplayMapper.toDTO(invitation)).thenReturn(invitationDto);

        ResponseEntity<List<CommunityInvitationDisplayDTO>> resp =
                controller.received(senderId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void received_negative() {
        when(communityInvitationService.getInvitationsByReceiverId(senderId))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> controller.received(senderId));
    }

    @Test
    void deleteOwn_positive() {
        doNothing().when(communityInvitationService).removeInvitation(
                new CommunityInvitationId(senderId, receiverId, communityId));

        ResponseEntity<Void> resp =
                controller.deleteOwn(communityId, receiverId, senderId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void deleteOwn_negative() {
        doThrow(new EntityNotFoundException("not found"))
                .when(communityInvitationService)
                .removeInvitation(any());

        assertThrows(EntityNotFoundException.class,
                () -> controller.deleteOwn(communityId, receiverId, senderId));
    }

    @Test
    void deleteAny_positive() {
        when(permissionCheckService.canUserDeleteCommunityInvitation(
                senderId,
                CommunityInvitationId.builder()
                        .senderId(senderId + 1)
                        .receiverId(receiverId)
                        .communityId(communityId)
                        .build()))
                .thenReturn(true);

        doNothing().when(communityInvitationService).removeInvitation(
                new CommunityInvitationId(senderId + 1, receiverId, communityId));

        ResponseEntity<Void> resp =
                controller.deleteAny(communityId, senderId + 1, receiverId, senderId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void deleteAny_negative() {
        when(permissionCheckService.canUserDeleteCommunityInvitation(
                senderId,
                CommunityInvitationId.builder()
                        .senderId(senderId + 1)
                        .receiverId(receiverId)
                        .communityId(communityId)
                        .build()))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.deleteAny(communityId, senderId + 1, receiverId, senderId));
    }

    @Test
    void accept_positive() {
        doNothing().when(communityInvitationService).acceptInvitation(
                new CommunityInvitationId(senderId + 1, senderId, communityId));

        ResponseEntity<Void> resp =
                controller.accept(communityId, senderId + 1, senderId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void accept_negative() {
        doThrow(new RuntimeException("fail")).when(communityInvitationService)
                .acceptInvitation(any());

        assertThrows(RuntimeException.class,
                () -> controller.accept(communityId, senderId + 1, senderId));
    }

    @Test
    void decline_positive() {
        doNothing().when(communityInvitationService).declineInvitation(
                new CommunityInvitationId(senderId + 1, senderId, communityId));

        ResponseEntity<Void> resp =
                controller.decline(communityId, senderId + 1, senderId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void decline_negative() {
        doThrow(new RuntimeException("fail")).when(communityInvitationService)
                .declineInvitation(any());

        assertThrows(RuntimeException.class,
                () -> controller.decline(communityId, senderId + 1, senderId));
    }
}
