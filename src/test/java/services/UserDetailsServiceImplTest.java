package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.SystemRole;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.UserDetailsImpl;
import ru.pocgg.SNSApp.services.UserDetailsServiceImpl;
import ru.pocgg.SNSApp.services.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    UserService userService;
    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userName("alice")
                .creationDate(Instant.now())
                .birthDate(LocalDate.parse("1990-01-01"))
                .password("secret")
                .email("alice@example.com")
                .firstName("Alice")
                .secondName("Smith")
                .gender(Gender.FEMALE)
                .systemRole(SystemRole.MODERATOR)
                .deleted(false)
                .acceptingPrivateMsgs(true)
                .postsPublic(true)
                .banned(false)
                .build();
        user.setId(42);
    }

    @Test
    void loadUserByUsername_positive() {
        when(userService.getUserByUserName("alice")).thenReturn(user);

        UserDetails ud = userDetailsService.loadUserByUsername("alice");

        assertEquals("alice", ud.getUsername());
        assertEquals("secret", ud.getPassword());
        assertTrue(ud.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList())
                .contains("ROLE_MODERATOR"));
        assertTrue(ud instanceof UserDetailsImpl);
        UserDetailsImpl custom = (UserDetailsImpl) ud;
        assertEquals(42, custom.getId());
    }

    @Test
    void loadUserByUsername_negative() {
        when(userService.getUserByUserName("unknown"))
                .thenThrow(new UsernameNotFoundException("not found"));

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown"));
    }
}
