package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.pocgg.SNSApp.DTO.display.CommunityDisplayDTO;
import ru.pocgg.SNSApp.DTO.display.CommunityFacadeDisplayDTO;
import ru.pocgg.SNSApp.DTO.display.CommunityMemberDisplayDTO;
import ru.pocgg.SNSApp.DTO.display.PostDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.CommunityDisplayMapper;
import ru.pocgg.SNSApp.DTO.mappers.CommunityMemberDisplayMapper;
import ru.pocgg.SNSApp.DTO.mappers.PostDisplayMapper;
import ru.pocgg.SNSApp.controller.rest.CommunityFacadeRestController;
import ru.pocgg.SNSApp.model.Community;
import ru.pocgg.SNSApp.model.CommunityMember;
import ru.pocgg.SNSApp.model.Post;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.CommunityMemberService;
import ru.pocgg.SNSApp.services.CommunityService;
import ru.pocgg.SNSApp.services.PermissionCheckService;
import ru.pocgg.SNSApp.services.PostService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityFacadeRestControllerTest {

    @Mock
    private CommunityService communityService;

    @Mock
    private PostService postService;

    @Mock
    private CommunityMemberService communityMemberService;

    @Mock
    private CommunityDisplayMapper communityMapper;

    @Mock
    private PostDisplayMapper postMapper;

    @Mock
    private CommunityMemberDisplayMapper memberMapper;

    @Mock
    private PermissionCheckService permissionCheckService;

    @InjectMocks
    private CommunityFacadeRestController controller;

    private int userId;
    private int communityId;
    private Instant creationDate;
    private Community community;
    private CommunityDisplayDTO communityDto;
    private int post1Id;
    private int post2Id;
    private Post post1;
    private Post post2;
    private PostDisplayDTO postDto1;
    private CommunityMember member;
    private CommunityMemberDisplayDTO memberDto;
    private List<Post> posts;
    private List<CommunityMember> members;

    @BeforeEach
    void setUp() {
        userId = 1;
        communityId = 2;
        post1Id = 3;
        post2Id = 4;
        creationDate = Instant.now();

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

        communityDto = CommunityDisplayDTO.builder()
                .id(community.getId())
                .communityName("Test")
                .creationDate(creationDate.toString())
                .build();

        post1 = Post.communityPostBuilder()
                .ownerCommunity(community)
                .deleted(false)
                .creationDate(creationDate.plusSeconds(10))
                .build();
        post1.setId(post1Id);

        post2 = Post.communityPostBuilder()
                .ownerCommunity(community)
                .deleted(true)
                .creationDate(creationDate)
                .build();
        post2.setId(post2Id);

        postDto1 = PostDisplayDTO.builder()
                .id(post1.getId())
                .creationDate(creationDate.plusSeconds(10).toString())
                .build();

        member = CommunityMember.builder()
                .community(community)
                .member(ownerUser)
                .build();

        memberDto = CommunityMemberDisplayDTO.builder()
                .communityId(community.getId())
                .memberId(ownerUser.getId())
                .entryDate(creationDate.toString())
                .build();

        posts = Arrays.asList(post1, post2);
        members = Arrays.asList(member);
    }

    @Test
    void getCommunityFullProfile_positive() {
        when(permissionCheckService.canUserViewCommunity(userId, communityId)).thenReturn(true);

        when(communityService.getCommunityById(communityId)).thenReturn(community);

        when(communityMapper.toDTO(community)).thenReturn(communityDto);

        when(postService.getPostsByCommunityOwner(communityId)).thenReturn(posts);

        when(postMapper.toDTO(post1)).thenReturn(postDto1);

        when(communityMemberService.getMembersByCommunityId(communityId)).thenReturn(members);

        when(memberMapper.toDTO(member)).thenReturn(memberDto);

        ResponseEntity<CommunityFacadeDisplayDTO> resp =
                controller.getCommunityFullProfile(userId, communityId);

        assertEquals(200, resp.getStatusCodeValue());

        CommunityFacadeDisplayDTO body = resp.getBody();

        assertEquals(communityDto, body.getCommunity());

        assertEquals(1, body.getPosts().size());
        assertEquals(postDto1, body.getPosts().get(0));

        assertEquals(1, body.getMembers().size());
        assertEquals(memberDto, body.getMembers().get(0));
    }

    @Test
    void getCommunityFullProfile_negative() {
        when(permissionCheckService.canUserViewCommunity(userId, communityId)).thenReturn(true);

        when(communityService.getCommunityById(communityId))
                .thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class,
                () -> controller.getCommunityFullProfile(userId, communityId));
    }
}
