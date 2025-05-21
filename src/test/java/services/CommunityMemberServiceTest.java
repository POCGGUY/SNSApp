package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.*;
import ru.pocgg.SNSApp.services.DAO.interfaces.CommunityMemberServiceDAO;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityMemberServiceTest {

    @Mock
    private CommunityMemberServiceDAO dao;
    @Mock
    private CommunityService communityService;
    @Mock
    private UserService userService;
    @InjectMocks
    private CommunityMemberService service;

    private int commId;
    private int userId;
    private Instant now;
    private User user;
    private Community community;

    @BeforeEach
    void setUp() {
        commId = 1;
        userId = 2;
        now = Instant.now();
        user = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        user.setId(userId);
        community = Community.builder()
                .owner(user)
                .communityName("Test")
                .creationDate(Instant.now())
                .description("desc")
                .isPrivate(false)
                .deleted(false)
                .banned(false)
                .build();
        community.setId(commId);
    }

    @Test
    void createMember_positive() {
        when(userService.getUserById(userId)).thenReturn(user);
        when(communityService.getCommunityById(commId)).thenReturn(community);

        var result = service.createMember(commId, userId, now);

        assertNotNull(result);
        assertEquals(CommunityRole.MEMBER, result.getMemberRole());
        verify(dao).addMember(result);
    }

    @Test
    void createMember_negative() {
        when(communityService.getCommunityById(commId)).thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class, () -> service.createMember(commId, userId, now));
        verifyNoInteractions(dao);
    }

    @Test
    void createMember_withRole_positive() {
        when(userService.getUserById(userId)).thenReturn(user);
        when(communityService.getCommunityById(commId)).thenReturn(community);

        var result = service.createMember(commId, userId, now, CommunityRole.OWNER);

        assertNotNull(result);
        assertEquals(CommunityRole.OWNER, result.getMemberRole());
        verify(dao).addMember(result);
    }

    @Test
    void getMembersByCommunityId_positive() {
        CommunityMember communityMembers = CommunityMember.builder()
                .community(community)
                .member(user)
                .entryDate(now)
                .memberRole(CommunityRole.MEMBER)
                .build();
        when(dao.getMembersByCommunityId(commId)).thenReturn(List.of(communityMembers));

        var result = service.getMembersByCommunityId(commId);

        assertSame(communityMembers, result.get(0));
    }

    @Test
    void getMembersByCommunityId_negative() {
        when(dao.getMembersByCommunityId(commId)).thenReturn(Collections.emptyList());

        var result = service.getMembersByCommunityId(commId);

        assertTrue(result.isEmpty());
    }

    @Test
    void setMemberRole_positive() {
        CommunityMember cm = CommunityMember.builder()
                .community(community)
                .member(user)
                .entryDate(now)
                .memberRole(CommunityRole.MEMBER)
                .build();
        CommunityMemberId id = new CommunityMemberId(commId, userId);
        when(dao.getMemberById(id)).thenReturn(cm);

        service.setMemberRole(id, CommunityRole.MODERATOR);

        assertEquals(CommunityRole.MODERATOR, cm.getMemberRole());
    }

    @Test
    void setMemberRole_negative() {
        CommunityMemberId id = new CommunityMemberId(commId, userId);
        when(dao.getMemberById(id)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.setMemberRole(id, CommunityRole.OWNER));
    }

    @Test
    void getAllMembers_positive() {
        CommunityMember cm = CommunityMember.builder()
                .community(community)
                .member(user)
                .entryDate(now)
                .memberRole(CommunityRole.MEMBER)
                .build();
        when(dao.getAllMembers()).thenReturn(List.of(cm));

        var result = service.getAllMembers();

        assertSame(cm, result.get(0));
    }

    @Test
    void getAllMembers_negative() {
        when(dao.getAllMembers()).thenReturn(Collections.emptyList());

        var result = service.getAllMembers();

        assertTrue(result.isEmpty());
    }

    @Test
    void getMemberById_positive() {
        CommunityMember cm = CommunityMember.builder()
                .community(community)
                .member(user)
                .entryDate(now)
                .memberRole(CommunityRole.MEMBER)
                .build();
        CommunityMemberId id = new CommunityMemberId(commId, userId);
        when(dao.getMemberById(id)).thenReturn(cm);

        var result = service.getMemberById(id);

        assertSame(cm, result);
    }

    @Test
    void getMemberById_negative() {
        CommunityMemberId id = new CommunityMemberId(commId, userId);
        when(dao.getMemberById(id)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.getMemberById(id));
    }

    @Test
    void removeMember_positive() {
        CommunityMember cm = CommunityMember.builder()
                .community(community)
                .member(user)
                .entryDate(now)
                .memberRole(CommunityRole.MEMBER)
                .build();
        CommunityMemberId id = new CommunityMemberId(commId, userId);
        when(dao.getMemberById(id)).thenReturn(cm);

        service.removeMember(id);

        verify(dao).removeMember(cm);
    }

    @Test
    void removeMember_negative() {
        CommunityMemberId id = new CommunityMemberId(commId, userId);
        when(dao.getMemberById(id)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.removeMember(id));
    }

    @Test
    void isMemberExist_positive() {
        CommunityMember cm = CommunityMember.builder()
                .community(community)
                .member(user)
                .entryDate(now)
                .memberRole(CommunityRole.MEMBER)
                .build();
        CommunityMemberId id = new CommunityMemberId(commId, userId);
        when(dao.getMemberById(id)).thenReturn(cm);

        assertTrue(service.isMemberExist(id));
    }

    @Test
    void isMemberExist_negative() {
        CommunityMemberId id = new CommunityMemberId(commId, userId);
        when(dao.getMemberById(id)).thenReturn(null);

        assertFalse(service.isMemberExist(id));
    }
}
