package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.pocgg.SNSApp.controller.rest.UserCommunitiesRestController;
import ru.pocgg.SNSApp.DTO.display.CommunityDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.CommunityDisplayMapper;
import ru.pocgg.SNSApp.model.Community;
import ru.pocgg.SNSApp.services.CommunityService;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommunitiesRestControllerTest {

    @Mock
    private CommunityService communityService;

    @Mock
    private CommunityDisplayMapper communityDisplayMapper;

    @InjectMocks
    private UserCommunitiesRestController controller;

    private int currentUserId;
    private int otherUserId;
    private Instant creationDate;
    private Community community;
    private CommunityDisplayDTO communityDto;

    @BeforeEach
    void setUp() {
        currentUserId = 1;
        otherUserId = 2;
        creationDate = Instant.now();

        community = Community.builder()
                .owner(null)
                .communityName("Test")
                .creationDate(Instant.now())
                .description("desc")
                .isPrivate(false)
                .deleted(false)
                .banned(false)
                .build();
        community.setId(10);

        communityDto = CommunityDisplayDTO.builder()
                .id(10)
                .communityName("TestCommunity")
                .build();
    }

    @Test
    void listUserCommunities_positive() {
        when(communityService.getCommunitiesByMemberId(currentUserId))
                .thenReturn(List.of(community));

        when(communityDisplayMapper.toDTO(community))
                .thenReturn(communityDto);

        ResponseEntity<List<CommunityDisplayDTO>> resp =
                controller.listUserCommunities(currentUserId);

        assertEquals(200, resp.getStatusCodeValue());
        List<CommunityDisplayDTO> body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(communityDto, body.get(0));
    }

    @Test
    void listUserCommunities_negative() {
        when(communityService.getCommunitiesByMemberId(currentUserId))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<CommunityDisplayDTO>> resp =
                controller.listUserCommunities(currentUserId);

        assertEquals(200, resp.getStatusCodeValue());
        List<CommunityDisplayDTO> body = resp.getBody();
        assertNotNull(body);
        assertTrue(body.isEmpty());
    }

    @Test
    void listOtherUserCommunities_positive() {
        when(communityService.getCommunitiesByMemberId(otherUserId))
                .thenReturn(List.of(community));

        when(communityDisplayMapper.toDTO(community))
                .thenReturn(communityDto);

        ResponseEntity<List<CommunityDisplayDTO>> resp =
                controller.listOtherUserCommunities(otherUserId);

        assertEquals(200, resp.getStatusCodeValue());
        List<CommunityDisplayDTO> body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(communityDto, body.get(0));
    }

    @Test
    void listOtherUserCommunities_negative() {
        when(communityService.getCommunitiesByMemberId(otherUserId))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<CommunityDisplayDTO>> resp =
                controller.listOtherUserCommunities(otherUserId);

        assertEquals(200, resp.getStatusCodeValue());
        List<CommunityDisplayDTO> body = resp.getBody();
        assertNotNull(body);
        assertTrue(body.isEmpty());
    }
}
