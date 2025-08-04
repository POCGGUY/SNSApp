package ru.pocgg.SNSApp.events.events;

import lombok.*;
import ru.pocgg.SNSApp.model.ChatInvitationId;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ChatInvitationDeclinedEvent {
    private ChatInvitationId id;
}
