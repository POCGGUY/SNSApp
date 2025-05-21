package ru.pocgg.SNSApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "community_members")
public class CommunityMember {

    @EmbeddedId
    private CommunityMemberId id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("communityId")
    @JoinColumn(name = "communityId", referencedColumnName = "id")
    private Community community;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "memberId", referencedColumnName = "id")
    private User member;

    @Column(nullable = false)
    private Instant entryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunityRole memberRole;

    @Builder
    public CommunityMember(Community community,
                           User member,
                           Instant entryDate,
                           CommunityRole memberRole) {
        this.id = CommunityMemberId.builder()
                .communityId(community.getId())
                .memberId(member.getId())
                .build();
        this.community = community;
        this.member = member;
        this.entryDate = entryDate;
        this.memberRole = memberRole;
    }

    public Boolean isModerator(){
        return memberRole == CommunityRole.MODERATOR || memberRole == CommunityRole.OWNER;
    }
}
