package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.services.*;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.FriendshipServiceDAO;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipServiceDAO dao;

    @Mock
    private UserService userService;

    @InjectMocks
    private FriendshipService service;

    private User user1;
    private User user2;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        user1 = User.builder()
                .userName("a").creationDate(now)
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        user1.setId(1);
        user2 = User.builder()
                .userName("b").creationDate(now)
                .birthDate(LocalDate.now().minusYears(25))
                .password("p").email("b@b")
                .firstName("B").secondName("B")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        user2.setId(2);
    }

    @Test
    void createFriendship_positive() {
        when(userService.getUserById(1)).thenReturn(user1);
        when(userService.getUserById(2)).thenReturn(user2);

        Friendship result = service.createFriendship(1, 2, now);

        ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);
        verify(dao).addFriendship(captor.capture());
        verify(dao).forceFlush();

        Friendship saved = captor.getValue();
        assertEquals(user1, saved.getUser());
        assertEquals(user2, saved.getFriend());
        assertEquals(now, saved.getCreationDate());
        assertEquals(saved, result);
    }

    @Test
    void createFriendship_negative() {
        when(userService.getUserById(1)).thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class,
                () -> service.createFriendship(1, 2, now));
        verifyNoInteractions(dao);
    }

    @Test
    void getUserFriendships_positive() {
        Friendship friendship1 = Friendship.builder().user(user1).friend(user2).creationDate(now).build();
        Friendship friendship2 = Friendship.builder().user(user2).friend(user1).creationDate(now).build();
        when(dao.getFriendshipsByUserId(1)).thenReturn(List.of(friendship1));
        when(dao.getFriendshipsByFriendId(1)).thenReturn(List.of(friendship2));

        List<Friendship> list = service.getUserFriendships(1);

        assertEquals(2, list.size());
        assertTrue(list.contains(friendship1) && list.contains(friendship2));
    }

    @Test
    void getUserFriendships_negative() {
        when(dao.getFriendshipsByUserId(3)).thenReturn(List.of());
        when(dao.getFriendshipsByFriendId(3)).thenReturn(List.of());

        List<Friendship> list = service.getUserFriendships(3);
        assertTrue(list.isEmpty());
    }

    @Test
    void removeFriendship_positive() {
        Friendship friendship = Friendship.builder().user(user1).friend(user2).creationDate(now).build();
        FriendshipId id = FriendshipId.builder().userId(1).friendId(2).build();
        when(dao.getFriendshipByEmbeddedId(id)).thenReturn(friendship);

        service.removeFriendship(1, 2);

        verify(dao).removeFriendship(friendship);
    }

    @Test
    void removeFriendship_negative() {
        FriendshipId id = FriendshipId.builder().userId(1).friendId(2).build();
        when(dao.getFriendshipByEmbeddedId(id)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.removeFriendship(1, 2));
        verify(dao, never()).removeFriendship(any());
    }

    @Test
    void isFriendshipExist_direct_positive() {
        FriendshipId id = FriendshipId.builder().userId(1).friendId(2).build();
        Friendship friendship = Friendship.builder().user(user1).friend(user2).creationDate(now).build();
        when(dao.getFriendshipByEmbeddedId(id)).thenReturn(friendship);

        assertTrue(service.isFriendshipExist(1, 2));
    }

    @Test
    void isFriendshipExist_reverse_positive() {
        FriendshipId direct = FriendshipId.builder().userId(1).friendId(2).build();
        FriendshipId reverse = FriendshipId.builder().userId(2).friendId(1).build();
        when(dao.getFriendshipByEmbeddedId(direct)).thenReturn(null);
        when(dao.getFriendshipByEmbeddedId(reverse)).thenReturn(Friendship.builder()
                .user(user1).friend(user2).creationDate(now).build());

        assertTrue(service.isFriendshipExist(1, 2));
    }

    @Test
    void isFriendshipExist_negative() {
        FriendshipId direct = FriendshipId.builder().userId(1).friendId(2).build();
        FriendshipId reverse = FriendshipId.builder().userId(2).friendId(1).build();
        when(dao.getFriendshipByEmbeddedId(direct)).thenReturn(null);
        when(dao.getFriendshipByEmbeddedId(reverse)).thenReturn(null);

        assertFalse(service.isFriendshipExist(1, 2));
    }

    @Test
    void getFriendshipByEmbeddedId_direct_positive() {
        FriendshipId id = FriendshipId.builder().userId(1).friendId(2).build();
        Friendship f = Friendship.builder().user(user1).friend(user2).creationDate(now).build();
        when(dao.getFriendshipByEmbeddedId(id)).thenReturn(f);

        assertSame(f, service.getFriendshipByEmbeddedId(1, 2));
    }

    @Test
    void getFriendshipByEmbeddedId_reverse_positive() {
        FriendshipId direct = FriendshipId.builder().userId(1).friendId(2).build();
        FriendshipId reverse = FriendshipId.builder().userId(2).friendId(1).build();
        Friendship f = Friendship.builder().user(user1).friend(user2).creationDate(now).build();
        when(dao.getFriendshipByEmbeddedId(direct)).thenReturn(null);
        when(dao.getFriendshipByEmbeddedId(reverse)).thenReturn(f);

        assertSame(f, service.getFriendshipByEmbeddedId(1, 2));
    }

    @Test
    void getFriendshipByEmbeddedId_negative() {
        FriendshipId direct = FriendshipId.builder().userId(1).friendId(2).build();
        FriendshipId reverse = FriendshipId.builder().userId(2).friendId(1).build();
        when(dao.getFriendshipByEmbeddedId(direct)).thenReturn(null);
        when(dao.getFriendshipByEmbeddedId(reverse)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.getFriendshipByEmbeddedId(1, 2));
    }
}
