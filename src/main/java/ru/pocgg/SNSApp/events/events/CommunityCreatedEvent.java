package ru.pocgg.SNSApp.events.events;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CommunityCreatedEvent {
    private final int communityId;
    private final int ownerId;
}
