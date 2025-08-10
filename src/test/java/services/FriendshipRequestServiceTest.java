package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.events.events.FriendshipRequestAcceptedEvent;
import ru.pocgg.SNSApp.events.events.FriendshipRequestCreatedEvent;
import ru.pocgg.SNSApp.events.events.FriendshipRequestDeclinedEvent;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.FriendshipRequestServiceDAO;
import ru.pocgg.SNSApp.services.FriendshipRequestService;
import ru.pocgg.SNSApp.services.UserService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipRequestServiceTest {

    @Mock
    private FriendshipRequestServiceDAO dao;
    @Mock
    private UserService userService;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private FriendshipRequestService service;

    private User sender;
    private User receiver;
    private FriendshipRequestId requestId;
    private FriendshipRequest request;

    @BeforeEach
    void setUp() {
        sender = mock(User.class);
        sender.setId(1);
        receiver = mock(User.class);
        receiver.setId(2);

        requestId = FriendshipRequestId.builder()
                .senderId(1)
                .receiverId(2)
                .build();

        request = FriendshipRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .creationDate(Instant.now())
                .build();
    }

    @Test
    void createRequest_positive() {
        when(userService.getUserById(1)).thenReturn(sender);
        when(userService.getUserById(2)).thenReturn(receiver);

        FriendshipRequest result = service.createRequest(1, 2, Instant.now());

        assertNotNull(result);
        verify(dao).addRequest(any(FriendshipRequest.class));
    }

    @Test
    void createRequest_negative() {
        when(userService.getUserById(1)).thenThrow(new EntityNotFoundException("User not found"));

        assertThrows(EntityNotFoundException.class, () -> service.createRequest(1, 2, Instant.now()));
        verify(dao, never()).addRequest(any());
    }

    @Test
    void getRequestsBySenderId_positive() {
        when(dao.getRequestsBySenderId(1)).thenReturn(List.of(request));

        List<FriendshipRequest> results = service.getRequestsBySenderId(1);
        assertEquals(1, results.size());
        assertEquals(request, results.get(0));
    }

    @Test
    void getRequestsBySenderId_negative() {
        when(dao.getRequestsBySenderId(1)).thenReturn(Collections.emptyList());

        List<FriendshipRequest> results = service.getRequestsBySenderId(1);
        assertTrue(results.isEmpty());
    }

    @Test
    void getRequestsByReceiverId_positive() {
        when(dao.getRequestsByReceiverId(2)).thenReturn(List.of(request));

        List<FriendshipRequest> results = service.getRequestsByReceiverId(2);
        assertEquals(1, results.size());
    }

    @Test
    void getRequestsByReceiverId_negative() {
        when(dao.getRequestsByReceiverId(2)).thenReturn(Collections.emptyList());

        List<FriendshipRequest> results = service.getRequestsByReceiverId(2);
        assertTrue(results.isEmpty());
    }

    @Test
    void removeBySenderId_positive() {
        service.removeBySenderId(1);

        verify(dao).removeBySenderId(1);
    }

    @Test
    void removeByReceiverId_positive() {
        service.removeByReceiverId(2);

        verify(dao).removeByReceiverId(2);
    }

    @Test
    void getAllRequests_positive() {
        when(dao.getAllRequests()).thenReturn(List.of(request));

        assertEquals(1, service.getAllRequests().size());
    }

    @Test
    void getAllRequests_negative() {
        when(dao.getAllRequests()).thenReturn(Collections.emptyList());

        assertTrue(service.getAllRequests().isEmpty());
    }

    @Test
    void getRequestById_positive() {
        when(dao.getRequestById(requestId)).thenReturn(request);

        assertEquals(request, service.getRequestById(requestId));
    }

    @Test
    void getRequestById_negative() {
        when(dao.getRequestById(requestId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.getRequestById(requestId));
    }

    @Test
    void isRequestExists_positive() {
        when(dao.getRequestById(any())).thenReturn(request);

        assertTrue(service.isRequestExists(requestId));
    }

    @Test
    void isRequestExists_negative() {
        when(dao.getRequestById(any())).thenReturn(null);

        assertFalse(service.isRequestExists(requestId));
    }

    @Test
    void acceptRequest_positive() {
        when(dao.getRequestById(requestId)).thenReturn(request);

        service.acceptRequest(requestId);

        verify(dao).removeRequest(request);
    }

    @Test
    void acceptRequest_negative() {
        when(dao.getRequestById(requestId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.acceptRequest(requestId));
    }

    @Test
    void declineRequest_positive() {
        when(dao.getRequestById(requestId)).thenReturn(request);

        service.declineRequest(requestId);

        verify(dao).removeRequest(request);
    }

    @Test
    void declineRequest_negative() {
        when(dao.getRequestById(requestId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.declineRequest(requestId));
    }

    @Test
    void deleteRequest_positive() {
        when(dao.getRequestById(requestId)).thenReturn(request);

        service.deleteRequest(requestId);

        verify(dao).removeRequest(request);
    }

    @Test
    void deleteRequest_negative() {
        when(dao.getRequestById(requestId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.deleteRequest(requestId));
    }
}