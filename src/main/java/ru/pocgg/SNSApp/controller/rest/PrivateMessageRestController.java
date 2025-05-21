package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.DTO.create.CreatePrivateMessageDTO;
import ru.pocgg.SNSApp.DTO.display.PrivateMessageDisplayDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePrivateMessageDTO;
import ru.pocgg.SNSApp.DTO.mappers.PrivateMessageDisplayMapper;
import ru.pocgg.SNSApp.model.PrivateMessage;
import ru.pocgg.SNSApp.services.PermissionCheckService;
import ru.pocgg.SNSApp.services.PrivateMessageService;
import ru.pocgg.SNSApp.services.UserService;

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
    private final PermissionCheckService permissionCheckService;
    private final UserService userService;

    @Operation(summary = "Отправить личное сообщение")
    @PostMapping("/send/{receiverId}")
    public ResponseEntity<PrivateMessageDisplayDTO> create(@AuthenticationPrincipal(expression = "id") int userId,
                                                           @PathVariable int receiverId,
                                                           @Valid @RequestBody CreatePrivateMessageDTO dto) {

        checkCanSend(userId, receiverId);
        PrivateMessage msg = privateMessageService.createMessage(userId, receiverId, dto);
        PrivateMessageDisplayDTO body = messageDisplayMapper.toDTO(msg);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Operation(summary = "Получить все личные сообщения между вами и определённым пользователем")
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
    @GetMapping("/{id}")
    public ResponseEntity<PrivateMessageDisplayDTO> getById(@PathVariable int id,
                                                            @AuthenticationPrincipal(expression = "id") int userId) {

        PrivateMessage msg = privateMessageService.getById(id);
        checkCanRead(userId, id);
        return ResponseEntity.ok(messageDisplayMapper.toDTO(msg));
    }

    @Operation(summary = "Редактировать текст личного сообщения")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> edit(@PathVariable int id,
                                     @Valid @RequestBody UpdatePrivateMessageDTO dto,
                                     @AuthenticationPrincipal(expression = "id") int userId) {

        checkCanModify(userId, id);
        privateMessageService.updateMessage(id, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить личное сообщение")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id,
                                       @AuthenticationPrincipal(expression = "id") int userId) {
        checkCanDelete(userId, id);
        privateMessageService.setDeleted(id, true);
        return ResponseEntity.noContent().build();
    }

    private void checkCanSend(int senderId, int receiverId) {
        if (!permissionCheckService.canSendMessageToThisUser(senderId, receiverId)) {
            throw new AccessDeniedException("You are not allowed to send message to this user");
        }
    }

    private void checkCanRead(int userId, int messageId) {
        if (!permissionCheckService.canUserReadPrivateMessage(userId, messageId)) {
            throw new AccessDeniedException("You are not authorized to read private message with this id");
        }
    }

    private void checkCanModify(int userId, int messageId) {
        if (!permissionCheckService.canUserModifyPrivateMessage(userId, messageId)) {
            throw new AccessDeniedException("You are not authorized to edit private message with this id");
        }
    }

    private void checkCanDelete(int userId, int messageId) {
        if (!permissionCheckService.canUserDeletePrivateMessage(userId, messageId)) {
            throw new AccessDeniedException("You are not authorized to delete private message with this id");
        }
    }
}

