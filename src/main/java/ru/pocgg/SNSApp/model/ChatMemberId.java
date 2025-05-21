package ru.pocgg.SNSApp.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ChatMemberId implements Serializable {
    private int chatId;
    private int memberId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMemberId)) return false;
        ChatMemberId that = (ChatMemberId) o;
        return Objects.equals(chatId, that.chatId) &&
                Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, memberId);
    }
}
