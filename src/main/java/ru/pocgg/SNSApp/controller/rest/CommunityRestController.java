package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.DTO.display.UserDisplayDTO;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.DTO.display.CommunityDisplayDTO;
import ru.pocgg.SNSApp.DTO.create.CreateCommunityDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateCommunityDTO;
import ru.pocgg.SNSApp.DTO.mappers.CommunityDisplayMapper;
import ru.pocgg.SNSApp.services.CommunityService;
import ru.pocgg.SNSApp.services.PermissionCheckService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Community", description = "Управление сообществами")
@RequestMapping("/communities")
public class CommunityRestController extends TemplateController {

    private final CommunityService communityService;
    private final CommunityDisplayMapper communityDisplayMapper;
    private final PermissionCheckService permissionCheckService;

    @Operation(summary = "Получить сообщество по id")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<CommunityDisplayDTO> getCommunity(@AuthenticationPrincipal(expression = "id") int userId,
                                                            @PathVariable int id) {
        checkCanViewCommunity(userId, id);
        Community community = communityService.getCommunityById(id);
        return ResponseEntity.ok(communityDisplayMapper.toDTO(community));
    }

    @Operation(summary = "Создать сообщество")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<Void> createCommunity(@AuthenticationPrincipal(expression = "id") int userId,
                                                @Valid @RequestBody CreateCommunityDTO dto) {
        communityService.createCommunity(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Редактировать сообщество")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> editCommunity(@AuthenticationPrincipal(expression = "id") int userId,
                                              @PathVariable int id,
                                              @Valid @RequestBody UpdateCommunityDTO dto) {
        checkCanEditCommunity(userId, id);
        communityService.updateCommunity(id, dto);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Активировать/деактивировать сообщество")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{id}/deleted")
    public ResponseEntity<Void> setDeleted(@PathVariable int id,
                                           @AuthenticationPrincipal(expression = "id") int userId,
                                           @RequestParam boolean value) {
        checkCanSetDeleted(userId, id);
        communityService.setIsDeleted(id, value);
        logger.info("user with id: {}, has set deleted to: {}, in community with id: {}",
                userId, value, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Заблокировать сообщество")
    @PreAuthorize("hasRole('MODERATOR')")
    @PatchMapping("/{id}/banned")
    public ResponseEntity<Void> setBanned(@PathVariable int id,
                                          @AuthenticationPrincipal(expression = "id") int userId,
                                          @RequestParam boolean value) {
        communityService.setIsBanned(id, value);
        logger.info("user with id: {}, has set banned to: {}, in community with id: {}", userId, value, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Поиск сообществ по названию")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/search")
    public ResponseEntity<List<CommunityDisplayDTO>> searchCommunities(@RequestParam String name) {
        List<Community> result = communityService.searchCommunities(name);
        List<CommunityDisplayDTO> DTOs = result.stream().map(communityDisplayMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(DTOs);
    }

    @Operation(summary = "Получить все сообщества (Только для модераторов и выше)")
    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("/all")
    public ResponseEntity<List<CommunityDisplayDTO>> getAllCommunities() {
        List<Community> result = communityService.getAllCommunities();
        List<CommunityDisplayDTO> DTOs = result.stream().map(communityDisplayMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(DTOs);
    }


    private void checkCanViewCommunity(int userId, int communityId) {
        if (!permissionCheckService.canUserViewCommunity(userId, communityId)) {
            throw new AccessDeniedException("You do not have permission to get this community");
        }
    }

    private void checkCanEditCommunity(int userId, int communityId) {
        if (!permissionCheckService.canUserEditCommunity(userId, communityId)) {
            throw new AccessDeniedException("You do not have permission to edit this community");
        }
    }

    private void checkCanSetDeleted(int userId, int communityId) {
        if (!permissionCheckService.isUserCommunityOwner(userId, communityId)) {
            throw new AccessDeniedException("You do not have permission to change deleted status for this community");
        }
    }
}


