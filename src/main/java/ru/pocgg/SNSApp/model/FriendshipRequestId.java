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
public class FriendshipRequestId implements Serializable {
    private Integer senderId;
    private Integer receiverId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendshipRequestId)) return false;
        FriendshipRequestId that = (FriendshipRequestId) o;
        return Objects.equals(senderId, that.senderId) &&
                Objects.equals(receiverId, that.receiverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, receiverId);
    }
}
