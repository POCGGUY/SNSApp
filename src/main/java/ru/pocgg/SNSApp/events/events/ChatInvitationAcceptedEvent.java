package ru.pocgg.SNSApp.events.events;

import lombok.*;
import ru.pocgg.SNSApp.model.ChatInvitationId;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ChatInvitationAcceptedEvent {
    private final ChatInvitationId id;
}
