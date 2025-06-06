package ru.pocgg.SNSApp.events.events;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ChatCreatedEvent {
    private int chatId;
    private int ownerId;
}
