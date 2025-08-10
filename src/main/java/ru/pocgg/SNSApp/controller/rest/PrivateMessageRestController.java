package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.DTO.create.CreatePrivateMessageDTO;
import ru.pocgg.SNSApp.DTO.display.PrivateMessageDisplayDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePrivateMessageDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.PrivateMessageDisplayMapper;
import ru.pocgg.SNSApp.model.PrivateMessage;
import ru.pocgg.SNSApp.services.PrivateMessageService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@Tag(name = "Private Message", description = "Управление приватными сообщениями")
@RequestMapping("/privateMessages")
public class PrivateMessageRestController {
    private final PrivateMessageService privateMessageService;
    private final PrivateMessageDisplayMapper messageDisplayMapper;

    @Operation(summary = "Отправить личное сообщение")
    @PreAuthorize("hasRole('USER') and @privateMessagePermissionService" +
            ".canSendPrivateMessage(principal.id, #receiverId)")
    @PostMapping("/send/{receiverId}")
    public ResponseEntity<PrivateMessageDisplayDTO> create(@AuthenticationPrincipal(expression = "id") int userId,
                                                           @PathVariable int receiverId,
                                                           @Valid @RequestBody CreatePrivateMessageDTO dto) {

        PrivateMessage msg = privateMessageService.createMessage(userId, receiverId, dto);
        PrivateMessageDisplayDTO body = messageDisplayMapper.toDTO(msg);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Operation(summary = "Получить все личные сообщения между вами и определённым пользователем")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/conversation/{partnerId}")
    public ResponseEntity<List<PrivateMessageDisplayDTO>> getMessagesFromTo(@AuthenticationPrincipal(expression = "id")
                                                                            int currentUserId,
                                                                            @PathVariable int partnerId) {
        List<PrivateMessage> sent = privateMessageService.getAllBySenderAndReceiver(currentUserId, partnerId);
        List<PrivateMessage> received = privateMessageService.getAllBySenderAndReceiver(partnerId, currentUserId);
        List<PrivateMessage> all = Stream
                .concat(sent.stream(), received.stream())
                .sorted(Comparator.comparing(PrivateMessage::getCreationDate))
                .toList();

        List<PrivateMessageDisplayDTO> dtos = all.stream()
                .map(messageDisplayMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Получить сообщение по ID")
    @PreAuthorize("hasRole('USER') and @privateMessagePermissionService.canReadMessage(principal.id, #id)")
    @GetMapping("/{id}")
    public ResponseEntity<PrivateMessageDisplayDTO> getById(@PathVariable int id) {
        PrivateMessage msg = privateMessageService.getById(id);
        return ResponseEntity.ok(messageDisplayMapper.toDTO(msg));
    }

    @Operation(summary = "Редактировать текст личного сообщения")
    @PreAuthorize("hasRole('USER') and @privateMessagePermissionService.canModifyMessage(principal.id, #id)")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> edit(@PathVariable int id,
                                     @Valid @RequestBody UpdatePrivateMessageDTO dto) {
        privateMessageService.updateMessage(id, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить личное сообщение")
    @PreAuthorize("hasRole('USER') and @privateMessagePermissionService.canDeleteMessage(principal.id, #id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        privateMessageService.setDeleted(id, true);
        return ResponseEntity.noContent().build();
    }
}

