package ru.pocgg.SNSApp.events.events;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CommunityCreatedEvent {
    private int communityId;
    private int ownerId;
}
