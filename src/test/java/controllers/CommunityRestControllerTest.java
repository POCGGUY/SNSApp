package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.create.CreateCommunityDTO;
import ru.pocgg.SNSApp.DTO.display.CommunityDisplayDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateCommunityDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.CommunityDisplayMapper;
import ru.pocgg.SNSApp.controller.rest.CommunityRestController;
import ru.pocgg.SNSApp.model.Community;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.CommunityService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityRestControllerTest {

    @Mock
    private CommunityService communityService;

    @Mock
    private PermissionCheckService permissionCheckService;

    @Mock
    private CommunityDisplayMapper communityDisplayMapper;

    @InjectMocks
    private CommunityRestController controller;

    private int userId;
    private int communityId;
    private Instant creationDate;
    private String communityName;
    private CreateCommunityDTO createDto;
    private UpdateCommunityDTO updateDto;
    private Community community;
    private CommunityDisplayDTO communityDto;
    private List<Community> communityList;
    private List<CommunityDisplayDTO> dtoList;

    @BeforeEach
    void setUp() {
        userId = 1;
        communityId = 2;
        creationDate = Instant.now();

        createDto = CreateCommunityDTO.builder()
                .communityName(communityName)
                .description("desc")
                .build();

        updateDto = UpdateCommunityDTO.builder()
                .communityName("NewName")
                .description("newDesc")
                .build();

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
                .communityName(createDto.getCommunityName())
                .creationDate(Instant.now())
                .description(createDto.getDescription())
                .isPrivate(false)
                .deleted(false)
                .banned(false)
                .build();
        community.setId(communityId);

        communityDto = CommunityDisplayDTO.builder()
                .id(communityId)
                .communityName(community.getCommunityName())
                .description(createDto.getDescription())
                .creationDate(creationDate.toString())
                .build();

        communityList = Collections.singletonList(community);
        dtoList = Collections.singletonList(communityDto);
    }

    @Test
    void getCommunity_positive() {
        when(permissionCheckService.canUserViewCommunity(userId, communityId)).thenReturn(true);

        when(communityService.getCommunityById(communityId)).thenReturn(community);

        when(communityDisplayMapper.toDTO(community)).thenReturn(communityDto);

        ResponseEntity<CommunityDisplayDTO> resp =
                controller.getCommunity(userId, communityId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(communityDto, resp.getBody());
    }

    @Test
    void getCommunity_negative() {
        when(permissionCheckService.canUserViewCommunity(userId, communityId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.getCommunity(userId, communityId));
    }

    @Test
    void createCommunity_positive() {
        when(communityService.createCommunity(userId, createDto)).thenReturn(community);

        ResponseEntity<Void> resp =
                controller.createCommunity(userId, createDto);

        assertEquals(201, resp.getStatusCodeValue());
    }

    @Test
    void createCommunity_negative() {
        doThrow(new RuntimeException("fail")).when(communityService).createCommunity(userId, createDto);

        assertThrows(RuntimeException.class,
                () -> controller.createCommunity(userId, createDto));
    }

    @Test
    void editCommunity_positive() {
        when(permissionCheckService.canUserEditCommunity(userId, communityId)).thenReturn(true);

        doNothing().when(communityService).updateCommunity(communityId, updateDto);

        ResponseEntity<Void> resp =
                controller.editCommunity(userId, communityId, updateDto);

        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void editCommunity_negative() {
        when(permissionCheckService.canUserEditCommunity(userId, communityId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.editCommunity(userId, communityId, updateDto));
    }

    @Test
    void setDeleted_positive() {
        when(permissionCheckService.isUserCommunityOwner(userId, communityId)).thenReturn(true);

        doNothing().when(communityService).setIsDeleted(communityId, true);

        ResponseEntity<Void> resp =
                controller.setDeleted(communityId, userId, true);

        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void setDeleted_negative() {
        when(permissionCheckService.isUserCommunityOwner(userId, communityId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.setDeleted(communityId, userId, false));
    }

    @Test
    void setBanned_positive() {
        doNothing().when(communityService).setIsBanned(communityId, true);

        ResponseEntity<Void> resp =
                controller.setBanned(communityId, userId, true);

        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void setBanned_negative() {
        doThrow(new EntityNotFoundException("not found")).when(communityService).setIsBanned(communityId, false);

        assertThrows(EntityNotFoundException.class,
                () -> controller.setBanned(communityId, userId, false));
    }

    @Test
    void searchCommunities_positive() {
        when(communityService.searchCommunities(communityName)).thenReturn(communityList);

        when(communityDisplayMapper.toDTO(community)).thenReturn(communityDto);

        ResponseEntity<List<CommunityDisplayDTO>> resp =
                controller.searchCommunities(communityName);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void searchCommunities_negative() {
        when(communityService.searchCommunities(communityName)).thenThrow(new RuntimeException("something went wrong"));

        assertThrows(RuntimeException.class,
                () -> controller.searchCommunities(communityName));
    }

    @Test
    void getAllCommunities_positive() {
        when(communityService.getAllCommunities()).thenReturn(communityList);

        when(communityDisplayMapper.toDTO(community)).thenReturn(communityDto);

        ResponseEntity<List<CommunityDisplayDTO>> resp =
                controller.getAllCommunities();

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void getAllCommunities_negative() {
        when(communityService.getAllCommunities()).thenThrow(new RuntimeException("something went wrong"));

        assertThrows(RuntimeException.class,
                () -> controller.getAllCommunities());
    }
}
