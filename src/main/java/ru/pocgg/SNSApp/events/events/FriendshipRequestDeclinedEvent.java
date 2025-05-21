package ru.pocgg.SNSApp.events.events;

import lombok.*;
import ru.pocgg.SNSApp.model.FriendshipRequestId;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class FriendshipRequestDeclinedEvent {
    private FriendshipRequestId id;
}
