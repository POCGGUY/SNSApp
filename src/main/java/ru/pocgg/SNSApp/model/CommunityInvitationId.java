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
public class CommunityInvitationId implements Serializable {
    private int senderId;
    private int receiverId;
    private int communityId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommunityInvitationId)) return false;
        CommunityInvitationId that = (CommunityInvitationId) o;
        return Objects.equals(senderId, that.senderId) &&
                Objects.equals(receiverId, that.receiverId) &&
                Objects.equals(communityId, that.communityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, receiverId, communityId);
    }
}
