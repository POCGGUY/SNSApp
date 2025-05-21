package ru.pocgg.SNSApp.events.events;

import lombok.*;
import ru.pocgg.SNSApp.model.Community;
import ru.pocgg.SNSApp.model.CommunityInvitationId;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CommunityInvitationAcceptedEvent {
    private final CommunityInvitationId id;
}
