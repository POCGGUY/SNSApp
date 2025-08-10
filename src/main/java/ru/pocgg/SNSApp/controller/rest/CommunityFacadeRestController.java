package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.model.Community;
import ru.pocgg.SNSApp.model.CommunityMember;
import ru.pocgg.SNSApp.DTO.display.CommunityFacadeDisplayDTO;
import ru.pocgg.SNSApp.model.Post;
import ru.pocgg.SNSApp.DTO.display.CommunityDisplayDTO;
import ru.pocgg.SNSApp.DTO.display.CommunityMemberDisplayDTO;
import ru.pocgg.SNSApp.DTO.display.PostDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.CommunityDisplayMapper;
import ru.pocgg.SNSApp.DTO.mappers.display.CommunityMemberDisplayMapper;
import ru.pocgg.SNSApp.DTO.mappers.display.PostDisplayMapper;
import ru.pocgg.SNSApp.services.CommunityMemberService;
import ru.pocgg.SNSApp.services.CommunityService;
import ru.pocgg.SNSApp.services.PostService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/facade/community")
@Tag(name = "Community Facade", description = "Информация о сообществе, его постах и участниках")
@RequiredArgsConstructor
public class CommunityFacadeRestController {
    private final CommunityService communityService;
    private final PostService postService;
    private final CommunityMemberService communityMemberService;
    private final CommunityDisplayMapper communityMapper;
    private final PostDisplayMapper postMapper;
    private final CommunityMemberDisplayMapper memberMapper;

    @GetMapping("/{communityId}")
    @PreAuthorize("hasRole('USER') and @communityPermissionService.canViewCommunity(principal.id, #communityId)")
    public ResponseEntity<CommunityFacadeDisplayDTO> getCommunityFullProfile(@PathVariable int communityId) {
        Community community = communityService.getCommunityById(communityId);
        CommunityDisplayDTO communityDTO = communityMapper.toDTO(community);
        List<PostDisplayDTO> postDTOs = getPostDTOsSortedByCreationDate
                (postService.getPostsByCommunityOwner(communityId));
        List<CommunityMemberDisplayDTO> memberDTOs = getMemberDTOs(communityId);

        return ResponseEntity.ok(new CommunityFacadeDisplayDTO(communityDTO, postDTOs, memberDTOs));
    }

    private List<PostDisplayDTO> getPostDTOsSortedByCreationDate(List<Post> posts) {
        return posts.stream()
                .filter(p -> !p.getDeleted())
                .sorted(Comparator.comparing(Post::getCreationDate).reversed())
                .map(postMapper::toDTO)
                .collect(Collectors.toList());
    }

    private List<CommunityMemberDisplayDTO> getMemberDTOs(int communityId) {
        List<CommunityMember> members = communityMemberService.getMembersByCommunityId(communityId);
        return members.stream()
                .map(memberMapper::toDTO)
                .collect(Collectors.toList());
    }
}
