package services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import ru.pocgg.SNSApp.DTO.create.CreateCommunityDTO;
import ru.pocgg.SNSApp.DTO.mappers.update.UpdateCommunityMapper;
import ru.pocgg.SNSApp.DTO.update.UpdateCommunityDTO;
import ru.pocgg.SNSApp.config.RabbitConfig;
import ru.pocgg.SNSApp.events.events.CommunityCreatedEvent;
import ru.pocgg.SNSApp.events.events.CommunityDeactivatedEvent;
import ru.pocgg.SNSApp.model.Community;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.SystemRole;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.CommunityService;
import ru.pocgg.SNSApp.services.DAO.interfaces.CommunityMemberServiceDAO;
import ru.pocgg.SNSApp.services.DAO.interfaces.CommunityServiceDAO;
import ru.pocgg.SNSApp.services.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @Mock
    private CommunityServiceDAO communityServiceDAO;
    @Mock
    private UserService userService;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private UpdateCommunityMapper updateCommunityMapper;
    @Mock
    private CommunityMemberServiceDAO communityMemberServiceDAO;

    @InjectMocks
    private CommunityService communityService;

    private User user;
    private Community community;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        user.setId(1);
        community = Community.builder()
                .owner(user)
                .communityName("Test")
                .creationDate(Instant.now())
                .description("desc")
                .isPrivate(false)
                .deleted(false)
                .banned(false)
                .build();
    }

    @Test
    void createCommunity_positive() {
        CreateCommunityDTO dto = CreateCommunityDTO.builder().communityName("Test").description("desc").isPrivate(false).build();
        when(userService.getUserById(1)).thenReturn(user);

        Community created = communityService.createCommunity(1, dto);

        assertNotNull(created);
        verify(communityServiceDAO).addCommunity(any());
    }

    @Test
    void createCommunity_negative() {
        when(userService.getUserById(1)).thenThrow(new EntityNotFoundException("Not found"));
        CreateCommunityDTO dto = CreateCommunityDTO.builder().communityName("Test").description("desc").isPrivate(false).build();

        assertThrows(EntityNotFoundException.class, () -> communityService.createCommunity(1, dto));
    }

    @Test
    void updateCommunity_positive() {
        UpdateCommunityDTO dto = UpdateCommunityDTO.builder().communityName("NewName").description("NewDesc").isPrivate(false).build();
        when(communityServiceDAO.getCommunityById(1)).thenReturn(community);

        communityService.updateCommunity(1, dto);

        verify(updateCommunityMapper).updateFromDTO(dto, community);
    }

    @Test
    void updateCommunity_negative() {
        when(communityServiceDAO.getCommunityById(1)).thenReturn(null);
        UpdateCommunityDTO dto = UpdateCommunityDTO.builder().build();

        assertThrows(EntityNotFoundException.class, () -> communityService.updateCommunity(1, dto));
    }

    @Test
    void getAllCommunities_positive() {
        when(communityServiceDAO.getAllCommunities()).thenReturn(List.of(community));

        List<Community> result = communityService.getAllCommunities();

        assertEquals(1, result.size());
    }

    @Test
    void getAllCommunities_negative() {
        when(communityServiceDAO.getAllCommunities()).thenReturn(Collections.emptyList());

        List<Community> result = communityService.getAllCommunities();

        assertTrue(result.isEmpty());
    }

    @Test
    void getCommunitiesByMemberId_positive() {
        when(communityMemberServiceDAO.getCommunitiesByMemberId(1)).thenReturn(List.of(community));

        List<Community> result = communityService.getCommunitiesByMemberId(1);

        assertEquals(1, result.size());
    }

    @Test
    void getCommunitiesByMemberId_negative() {
        when(communityMemberServiceDAO.getCommunitiesByMemberId(1)).thenReturn(Collections.emptyList());

        List<Community> result = communityService.getCommunitiesByMemberId(1);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCommunityById_positive() {
        when(communityServiceDAO.getCommunityById(1)).thenReturn(community);

        Community result = communityService.getCommunityById(1);

        assertNotNull(result);
    }

    @Test
    void getCommunityById_negative() {
        when(communityServiceDAO.getCommunityById(1)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> communityService.getCommunityById(1));
    }

    @Test
    void searchCommunities_positive() {
        when(communityServiceDAO.searchCommunities("Test")).thenReturn(List.of(community));

        List<Community> result = communityService.searchCommunities("Test");

        assertEquals(1, result.size());
    }

    @Test
    void searchCommunities_empty() {
        when(communityServiceDAO.searchCommunities("Test")).thenReturn(Collections.emptyList());

        List<Community> result = communityService.searchCommunities("Test");

        assertTrue(result.isEmpty());
    }

    @Test
    void setIsDeleted_positive() {
        when(communityServiceDAO.getCommunityById(1)).thenReturn(community);

        communityService.setIsDeleted(1, true);

        assertTrue(community.getDeleted());
    }

    @Test
    void setIsDeleted_noChange() {
        community.setDeleted(true);
        when(communityServiceDAO.getCommunityById(1)).thenReturn(community);

        communityService.setIsDeleted(1, true);
    }

    @Test
    void setIsBanned_positive() {
        when(communityServiceDAO.getCommunityById(1)).thenReturn(community);

        communityService.setIsBanned(1, true);

        assertTrue(community.getBanned());
    }

    @Test
    void setIsBanned_noChange() {
        community.setBanned(true);
        when(communityServiceDAO.getCommunityById(1)).thenReturn(community);

        communityService.setIsBanned(1, true);
    }
}