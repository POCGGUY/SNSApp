package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.DTO.display.NotificationDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.NotificationDisplayMapper;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.services.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Управление уведомлениями")
public class NotificationRestController extends TemplateController {
    private final NotificationService notificationService;
    private final NotificationDisplayMapper notificationDisplayMapper;

    @Operation(summary = "Получить все (непрочитанные) уведомления")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/incoming")
    public ResponseEntity<List<NotificationDisplayDTO>> getNotSeen(@AuthenticationPrincipal(expression = "id")
                                                                    int userId) {
        List<NotificationDisplayDTO> list =
                getDTOsSortedByCreationDate(notificationService.getNotSeenNotificationsByReceiverId(userId));
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Получить все уведомления")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<NotificationDisplayDTO>> getAll(@AuthenticationPrincipal(expression = "id") int userId) {
        List<NotificationDisplayDTO> list =
                getDTOsSortedByCreationDate(notificationService.getAllNotificationsByReceiverId(userId));
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Пометить уведомление как прочитанное/непрочитанное")
    @PreAuthorize("hasRole('USER') and @notificationPermissionService.canMarkRead(principal.id, #id)")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> setRead(@PathVariable int id,
                                        @RequestParam boolean value) {
        notificationService.setRead(id, value);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить уведомление")
    @PreAuthorize("hasRole('USER') and @notificationPermissionService.canDelete(principal.id, #id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id,
                                       @AuthenticationPrincipal(expression = "id") int userId) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private List<NotificationDisplayDTO> getDTOsSortedByCreationDate(List<Notification> notifications) {
        return notifications.stream()
                .sorted(Comparator.comparing(Notification::getCreationDate).reversed())
                .map(notificationDisplayMapper::toDTO)
                .collect(Collectors.toList());
    }
}
