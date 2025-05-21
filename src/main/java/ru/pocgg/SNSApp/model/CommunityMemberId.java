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
public class CommunityMemberId  implements Serializable {
    private int communityId;
    private int memberId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommunityMemberId)) return false;
        CommunityMemberId that = (CommunityMemberId) o;
        return Objects.equals(communityId, that.communityId) &&
                Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, communityId);
    }
}
