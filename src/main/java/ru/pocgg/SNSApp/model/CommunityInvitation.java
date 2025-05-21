package ru.pocgg.SNSApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "community_invitations")
public class CommunityInvitation {

    @EmbeddedId
    private CommunityInvitationId id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("senderId")
    @JoinColumn(name = "senderId", referencedColumnName = "id")
    private User sender;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("receiverId")
    @JoinColumn(name = "receiverId", referencedColumnName = "id")
    private User receiver;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("communityId")
    @JoinColumn(name = "communityId", referencedColumnName = "id")
    private Community community;

    @Column(nullable = false)
    private Instant creationDate;

    @Column(nullable = true)
    @Size(max = 1000)
    private String description;

    @Builder
    public CommunityInvitation(User sender,
                               User receiver,
                               Community community,
                               Instant creationDate,
                               String description) {
        this.id = CommunityInvitationId.builder()
                .senderId(sender.getId())
                .receiverId(receiver.getId())
                .communityId(community.getId())
                .build();
        this.sender = sender;
        this.receiver = receiver;
        this.community = community;
        this.creationDate = creationDate;
        this.description = description;
    }
}
