package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.display.FriendshipDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.FriendshipDisplayMapper;
import ru.pocgg.SNSApp.controller.rest.FriendshipRestController;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.FriendshipService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipRestControllerTest {

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private PermissionCheckService permissionCheckService;

    @Mock
    private FriendshipDisplayMapper friendshipDisplayMapper;

    @InjectMocks
    private FriendshipRestController controller;

    private int userId;
    private int friendId;
    private Friendship friendship;
    private FriendshipDisplayDTO dto;
    private List<Friendship> friendshipList;
    private List<FriendshipDisplayDTO> dtoList;

    @BeforeEach
    void setUp() {
        userId = 1;
        friendId = 2;

        User user = User.builder().userName("r").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("r@e").firstName("R").secondName("R")
                .gender(null).systemRole(null)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        user.setId(userId);

        User friend = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        friend.setId(friendId);

        friendship = Friendship.builder().user(user).friend(friend).build();
        friendship.setId(FriendshipId.builder().userId(userId).friendId(friendId).build());

        dto = FriendshipDisplayDTO.builder().build();

        friendshipList = Collections.singletonList(friendship);
        dtoList = Collections.singletonList(dto);
    }

    @Test
    void listFriendships_positive() {
        when(friendshipService.getUserFriendships(userId))
                .thenReturn(friendshipList);

        when(friendshipDisplayMapper.toDTO(friendship, userId))
                .thenReturn(dto);

        ResponseEntity<List<FriendshipDisplayDTO>> resp =
                controller.listFriendships(userId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void listFriendships_negative() {
        when(friendshipService.getUserFriendships(userId))
                .thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class,
                () -> controller.listFriendships(userId));
    }

    @Test
    void deleteFriendship_positive() {
        when(friendshipService.isFriendshipExist(userId, friendId))
                .thenReturn(true);

        ResponseEntity<Void> resp =
                controller.deleteFriendship(userId, friendId);

        assertEquals(204, resp.getStatusCodeValue());

        verify(friendshipService).removeFriendship(userId, friendId);
    }

    @Test
    void deleteFriendship_negative() {
        when(friendshipService.isFriendshipExist(userId, friendId))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.deleteFriendship(userId, friendId));
    }
}
