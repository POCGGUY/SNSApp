package ru.pocgg.SNSApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "friendship_requests")
public class FriendshipRequest {

    @EmbeddedId
    private FriendshipRequestId id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("senderId")
    @JoinColumn(name = "senderId", referencedColumnName = "id")
    private User sender;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("receiverId")
    @JoinColumn(name = "receiverId", referencedColumnName = "id")
    private User receiver;

    @Column(nullable = false)
    private Instant creationDate;

    @Builder
    public FriendshipRequest(User sender,
                             User receiver,
                             Instant creationDate) {
        this.id = FriendshipRequestId.builder()
                .senderId(sender.getId())
                .receiverId(receiver.getId())
                .build();
        this.sender = sender;
        this.receiver = receiver;
        this.creationDate = creationDate;
    }
}

