package ru.pocgg.SNSApp.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Embeddable
public class ChatInvitationId implements Serializable {
    private int senderId;
    private int receiverId;
    private int chatId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatInvitationId)) return false;
        ChatInvitationId that = (ChatInvitationId) o;
        return Objects.equals(senderId, that.senderId) &&
                Objects.equals(receiverId, that.receiverId) &&
                 Objects.equals(chatId, that.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, receiverId, chatId);
    }
}
