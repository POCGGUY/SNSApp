package ru.pocgg.SNSApp.events.events;

import lombok.*;
import ru.pocgg.SNSApp.model.FriendshipRequestId;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class FriendshipRequestCreatedEvent {
    private FriendshipRequestId id;
}
