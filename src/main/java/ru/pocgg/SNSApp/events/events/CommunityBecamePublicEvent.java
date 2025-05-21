package ru.pocgg.SNSApp.events.events;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CommunityBecamePublicEvent {
    private int communityId;
}
