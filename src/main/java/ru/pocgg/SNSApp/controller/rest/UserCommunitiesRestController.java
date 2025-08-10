package ru.pocgg.SNSApp.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ru.pocgg.SNSApp.model.Community;
import ru.pocgg.SNSApp.DTO.display.CommunityDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.CommunityDisplayMapper;
import ru.pocgg.SNSApp.services.CommunityService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users/")
@RequiredArgsConstructor
@Tag(name = "User Communities", description = "Сообщества, в которых состоит пользователь")
public class UserCommunitiesRestController {
    private final CommunityService communityService;
    private final CommunityDisplayMapper communityDisplayMapper;

    @Operation(summary = "Список сообществ, в которых вы состоите")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/communities")
    public ResponseEntity<List<CommunityDisplayDTO>> listUserCommunities(@AuthenticationPrincipal(expression = "id")
                                                                         int currentUserId) {
        List<CommunityDisplayDTO> communities = mapToDTOs(communityService.getCommunitiesByMemberId(currentUserId));
        return ResponseEntity.ok(communities);
    }

    @Operation(summary = "Список сообществ, в которых состоит другой пользователь (только для модераторов и выше)")
    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("/{userId}/communities")
    public ResponseEntity<List<CommunityDisplayDTO>> listOtherUserCommunities(@PathVariable int userId) {
        List<CommunityDisplayDTO> communities = mapToDTOs(communityService.getCommunitiesByMemberId(userId));
        return ResponseEntity.ok(communities);
    }

    private List<CommunityDisplayDTO> mapToDTOs(List<Community> list) {
        return list.stream()
                .map(communityDisplayMapper::toDTO)
                .collect(Collectors.toList());
    }
}
