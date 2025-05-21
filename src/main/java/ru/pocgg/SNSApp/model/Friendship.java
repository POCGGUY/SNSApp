package ru.pocgg.SNSApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "friendships")
public class Friendship {

    @EmbeddedId
    private FriendshipId id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "userId", referencedColumnName = "id")
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("friendId")
    @JoinColumn(name = "friendId", referencedColumnName = "id")
    private User friend;

    @Column(nullable = false)
    private Instant creationDate;

    @Builder
    public Friendship(User user,
                      User friend,
                      Instant creationDate) {
        this.id = FriendshipId.builder()
                .userId(user.getId())
                .friendId(friend.getId())
                .build();
        this.user = user;
        this.friend = friend;
        this.creationDate = creationDate;
    }
}
