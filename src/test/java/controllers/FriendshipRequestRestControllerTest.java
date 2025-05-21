package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.display.FriendshipRequestDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.FriendshipRequestDisplayMapper;
import ru.pocgg.SNSApp.controller.rest.FriendshipRequestRestController;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.model.exceptions.BadRequestException;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.FriendshipRequestService;
import ru.pocgg.SNSApp.services.FriendshipService;
import ru.pocgg.SNSApp.services.PermissionCheckService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipRequestRestControllerTest {

    @Mock
    private FriendshipRequestService friendshipRequestService;

    @Mock
    private PermissionCheckService permissionCheckService;

    @Mock
    private FriendshipRequestDisplayMapper friendshipRequestDisplayMapper;

    @Mock
    private FriendshipService friendshipService;

    @InjectMocks
    private FriendshipRequestRestController controller;

    private int userId;
    private int receiverId;
    private Instant creationDate;
    private FriendshipRequest request;
    private FriendshipRequestDisplayDTO requestDto;
    private List<FriendshipRequest> requestList;
    private List<FriendshipRequestDisplayDTO> dtoList;

    @BeforeEach
    void setUp() {
        userId = 1;
        receiverId = 2;
        creationDate = Instant.now();

        User sender = User.builder().userName("r").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("r@e").firstName("R").secondName("R")
                .gender(null).systemRole(null)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        sender.setId(userId);

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

        request = FriendshipRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .creationDate(creationDate)
                .build();

        request.setId(FriendshipRequestId.builder()
                .senderId(userId)
                .receiverId(receiverId)
                .build());

        requestDto = FriendshipRequestDisplayDTO.builder()
                .senderId(userId)
                .receiverId(receiverId)
                .creationDate(creationDate.toString())
                .build();

        requestList = Collections.singletonList(request);
        dtoList = Collections.singletonList(requestDto);
    }

    @Test
    void createRequest_positive() {
        when(permissionCheckService.canSendFriendRequest(receiverId)).thenReturn(true);

        when(friendshipRequestService.isRequestExists(
                FriendshipRequestId.builder().senderId(userId).receiverId(receiverId).build()))
                .thenReturn(false);

        when(friendshipService.isFriendshipExist(userId, receiverId)).thenReturn(false);

        when(friendshipRequestService.createRequest(eq(userId), eq(receiverId), any()))
                .thenReturn(request);

        when(friendshipRequestDisplayMapper.toDTO(request)).thenReturn(requestDto);

        ResponseEntity<FriendshipRequestDisplayDTO> resp =
                controller.createRequest(userId, receiverId);

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(requestDto, resp.getBody());
    }

    @Test
    void createRequest_negative() {
        assertThrows(BadRequestException.class,
                () -> controller.createRequest(userId, userId));
    }


    @Test
    void sent_positive() {
        when(friendshipRequestService.getRequestsBySenderId(userId))
                .thenReturn(requestList);

        when(friendshipRequestDisplayMapper.toDTO(request))
                .thenReturn(requestDto);

        ResponseEntity<List<FriendshipRequestDisplayDTO>> resp =
                controller.sent(userId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void sent_negative() {
        when(friendshipRequestService.getRequestsBySenderId(userId))
                .thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class,
                () -> controller.sent(userId));
    }

    @Test
    void received_positive() {
        when(friendshipRequestService.getRequestsByReceiverId(userId))
                .thenReturn(requestList);

        when(friendshipRequestDisplayMapper.toDTO(request))
                .thenReturn(requestDto);

        ResponseEntity<List<FriendshipRequestDisplayDTO>> resp =
                controller.received(userId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void received_negative() {
        when(friendshipRequestService.getRequestsByReceiverId(userId))
                .thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class,
                () -> controller.received(userId));
    }

    @Test
    void acceptRequest_positive() {
        doNothing().when(friendshipRequestService).acceptRequest(
                FriendshipRequestId.builder().senderId(receiverId).receiverId(userId).build());

        ResponseEntity<Void> resp =
                controller.acceptRequest(receiverId, userId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void acceptRequest_negative() {
        doThrow(new AccessDeniedException("you re not allowed to accept this")).when(friendshipRequestService)
                .acceptRequest(any());

        assertThrows(AccessDeniedException.class,
                () -> controller.acceptRequest(receiverId, userId));
    }

    @Test
    void declineRequest_positive() {
        doNothing().when(friendshipRequestService).declineRequest(
                FriendshipRequestId.builder().senderId(receiverId).receiverId(userId).build());

        ResponseEntity<Void> resp =
                controller.declineRequest(receiverId, userId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void declineRequest_negative() {
        doThrow(new AccessDeniedException("you re not allowed to decline this")).when(friendshipRequestService)
                .declineRequest(any());

        assertThrows(AccessDeniedException.class,
                () -> controller.declineRequest(receiverId, userId));
    }

    @Test
    void deleteRequest_positive() {
        doNothing().when(friendshipRequestService).deleteRequest(
                FriendshipRequestId.builder().senderId(userId).receiverId(receiverId).build());

        ResponseEntity<Void> resp =
                controller.deleteRequest(receiverId, userId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void deleteRequest_negative() {
        doThrow(new AccessDeniedException("you re not allowed to delete this")).when(friendshipRequestService)
                .deleteRequest(any());

        assertThrows(AccessDeniedException.class,
                () -> controller.deleteRequest(receiverId, userId));
    }
}
