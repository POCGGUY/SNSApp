package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ru.pocgg.SNSApp.DTO.create.CreateCommunityInvitationDTO;
import ru.pocgg.SNSApp.events.events.CommunityInvitationAcceptedEvent;
import ru.pocgg.SNSApp.events.events.CommunityInvitationCreatedEvent;
import ru.pocgg.SNSApp.events.events.CommunityInvitationDeclinedEvent;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.CommunityInvitationService;
import ru.pocgg.SNSApp.services.CommunityService;
import ru.pocgg.SNSApp.services.UserService;
import ru.pocgg.SNSApp.services.DAO.interfaces.CommunityInvitationServiceDAO;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityInvitationServiceTest {

    @Mock
    private CommunityInvitationServiceDAO dao;
    @Mock
    private UserService userService;
    @Mock
    private CommunityService communityService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private CommunityInvitationService service;

    private int senderId;
    private int receiverId;
    private int communityId;
    private Instant creationDate;
    private CreateCommunityInvitationDTO dto;
    private User sender;
    private User receiver;
    private Community community;
    private CommunityInvitationId id;
    private CommunityInvitation invitation;

    @BeforeEach
    void setUp() {
        senderId = 1;
        receiverId = 2;
        communityId = 3;
        creationDate = Instant.now();
        dto = new CreateCommunityInvitationDTO("hello");

        sender = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        sender.setId(senderId);

        receiver = User.builder()
                .userName("b").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(25))
                .password("p").email("b@b")
                .firstName("B").secondName("B")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        receiver.setId(receiverId);

        community = Community.builder()
                .owner(sender)
                .communityName("C")
                .creationDate(creationDate)
                .description("d")
                .isPrivate(false)
                .deleted(false)
                .banned(false)
                .build();
        community.setId(communityId);

        id = CommunityInvitationId.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .communityId(communityId)
                .build();

        invitation = CommunityInvitation.builder()
                .sender(sender)
                .receiver(receiver)
                .community(community)
                .creationDate(creationDate)
                .description(dto.getDescription())
                .build();
    }

    @Test
    void createInvitation_positive() {
        when(userService.getUserById(senderId)).thenReturn(sender);
        when(userService.getUserById(receiverId)).thenReturn(receiver);
        when(communityService.getCommunityById(communityId)).thenReturn(community);

        CommunityInvitation result = service.createInvitation(senderId, receiverId, communityId, creationDate, dto);

        assertNotNull(result);

        verify(dao).addInvitation(result);
        verify(eventPublisher).publishEvent(any(CommunityInvitationCreatedEvent.class));
    }

    @Test
    void createInvitation_negative() {
        when(userService.getUserById(senderId)).thenThrow(new EntityNotFoundException("no sender"));

        assertThrows(EntityNotFoundException.class,
                () -> service.createInvitation(senderId, receiverId, communityId, creationDate, dto));

        verifyNoInteractions(dao, eventPublisher);
    }

    @Test
    void isInvitationExist_positive() {
        when(dao.getInvitationById(id)).thenReturn(invitation);

        assertTrue(service.isInvitationExist(id));
    }

    @Test
    void isInvitationExist_negative() {
        when(dao.getInvitationById(id)).thenReturn(null);

        assertFalse(service.isInvitationExist(id));
    }

    @Test
    void getInvitationsBySenderId_positive() {
        List<CommunityInvitation> list = List.of(invitation);
        when(dao.getInvitationsBySenderId(senderId)).thenReturn(list);

        assertSame(list, service.getInvitationsBySenderId(senderId));
    }

    @Test
    void getInvitationsBySenderId_negative() {
        when(dao.getInvitationsBySenderId(senderId)).thenReturn(Collections.emptyList());

        List<CommunityInvitation> invitationsBySenderId = service.getInvitationsBySenderId(senderId);

        assertNotNull(invitationsBySenderId);
        assertTrue(invitationsBySenderId.isEmpty());
    }

    @Test
    void getInvitationsByReceiverId_positive() {
        List<CommunityInvitation> list = List.of(invitation);
        when(dao.getInvitationsByReceiverId(receiverId)).thenReturn(list);

        assertSame(list, service.getInvitationsByReceiverId(receiverId));
    }

    @Test
    void getInvitationsByReceiverId_negative() {
        when(dao.getInvitationsByReceiverId(receiverId)).thenReturn(Collections.emptyList());

        List<CommunityInvitation> invitationsByReceiverId = service.getInvitationsByReceiverId(receiverId);

        assertNotNull(invitationsByReceiverId);
        assertTrue(invitationsByReceiverId.isEmpty());
    }

    @Test
    void getInvitationsByCommunityId_positive() {
        List<CommunityInvitation> list = List.of(invitation);
        when(dao.getInvitationsByCommunityId(communityId)).thenReturn(list);

        assertSame(list, service.getInvitationsByCommunityId(communityId));
    }

    @Test
    void getInvitationsByCommunityId_negative() {
        when(dao.getInvitationsByCommunityId(communityId)).thenReturn(Collections.emptyList());

        List<CommunityInvitation> invitationsByCommunityId = service.getInvitationsByCommunityId(communityId);

        assertNotNull(invitationsByCommunityId);
        assertTrue(invitationsByCommunityId.isEmpty());
    }

    @Test
    void getInvitationById_positive() {
        when(dao.getInvitationById(id)).thenReturn(invitation);

        assertSame(invitation, service.getInvitationById(id));
    }

    @Test
    void getInvitationById_negative() {
        when(dao.getInvitationById(id)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.getInvitationById(id));
    }

    @Test
    void isUserAlreadyInvited_positive() {
        when(dao.getInvitationsByReceiverAndCommunityId(receiverId, communityId))
                .thenReturn(List.of(invitation));

        assertTrue(service.isUserAlreadyInvited(receiverId, communityId));
    }

    @Test
    void isUserAlreadyInvited_negative() {
        when(dao.getInvitationsByReceiverAndCommunityId(receiverId, communityId))
                .thenReturn(Collections.emptyList());

        assertFalse(service.isUserAlreadyInvited(receiverId, communityId));
    }

    @Test
    void acceptInvitation_positive() {
        when(dao.getInvitationById(id)).thenReturn(invitation);

        service.acceptInvitation(id);

        verify(eventPublisher).publishEvent(any(CommunityInvitationAcceptedEvent.class));
        verify(dao).removeInvitation(invitation);
    }

    @Test
    void acceptInvitation_negative() {
        when(dao.getInvitationById(id)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.acceptInvitation(id));
    }

    @Test
    void declineInvitation_positive() {
        when(dao.getInvitationById(id)).thenReturn(invitation);

        service.declineInvitation(id);

        verify(eventPublisher).publishEvent(any(CommunityInvitationDeclinedEvent.class));
        verify(dao).removeInvitation(invitation);
    }

    @Test
    void declineInvitation_negative() {
        when(dao.getInvitationById(id)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.declineInvitation(id));
    }

    @Test
    void removeInvitation_positive() {
        when(dao.getInvitationById(id)).thenReturn(invitation);

        service.removeInvitation(id);

        verify(dao).removeInvitation(invitation);
    }

    @Test
    void removeInvitation_negative() {
        when(dao.getInvitationById(id)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.removeInvitation(id));
    }

    @Test
    void removeBySenderId_positive() {
        service.removeBySenderId(senderId);

        verify(dao).removeBySenderId(senderId);
    }

    @Test
    void removeByReceiverId_positive() {
        service.removeByReceiverId(receiverId);

        verify(dao).removeByReceiverId(receiverId);
    }

    @Test
    void removeByChatId_positive() {
        service.removeByCommunityId(communityId);

        verify(dao).removeByCommunityId(communityId);
    }
}
