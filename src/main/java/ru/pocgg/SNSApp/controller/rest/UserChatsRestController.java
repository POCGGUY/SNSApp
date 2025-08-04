package ru.pocgg.SNSApp.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pocgg.SNSApp.DTO.display.ChatDisplayDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.DTO.mappers.display.ChatDisplayMapper;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.services.ChatService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users/")
@RequiredArgsConstructor
@Tag(name = "User Chats", description = "Чаты, в которых состоит пользователь")
public class UserChatsRestController {

    private final ChatService chatService;
    private final ChatDisplayMapper chatDisplayMapper;

    @Operation(summary = "Список чатов где вы состоите")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/chats")
    public ResponseEntity<List<ChatDisplayDTO>> listUserChats(@AuthenticationPrincipal(expression = "id")
                                                              int currentUserId) {
        List<ChatDisplayDTO> chats = getDTOs(chatService.getChatsByMemberId(currentUserId));
        return ResponseEntity.ok(chats);
    }

    @Operation(summary = "Список чатов где состоит другой пользователь(Только для модераторов и выше")
    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("/{userId}/chats")
    public ResponseEntity<List<ChatDisplayDTO>> listOtherUserChats(@PathVariable int userId) {
        List<ChatDisplayDTO> chats = getDTOs(chatService.getChatsByMemberId(userId));
        return ResponseEntity.ok(chats);
    }

    private List<ChatDisplayDTO> getDTOs(List<Chat> chats) {
        return chats.stream()
                .map(chatDisplayMapper::toDTO)
                .collect(Collectors.toList());
    }

}

