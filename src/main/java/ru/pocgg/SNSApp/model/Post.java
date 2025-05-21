package ru.pocgg.SNSApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.Instant;

@Entity
@Getter
@Setter
@Check(
        constraints = "(ownerUserId IS NOT NULL AND ownerCommunityId IS NULL) " +
                "OR (ownerUserId IS NULL AND ownerCommunityId IS NOT NULL)"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts")
public class Post{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "ownerUserId", referencedColumnName = "id")
    private User ownerUser;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "ownerCommunityId", referencedColumnName = "id")
    private Community ownerCommunity;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "authorId", referencedColumnName = "id")
    private User author;

    @Column(nullable = false)
    private Instant creationDate;

    @Column(nullable = false)
    private Instant updateDate;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(nullable = false)
    @NotBlank(message = "post's text should not be empty")
    private String text;

    @Builder(builderClassName = "UserPostBuilder", builderMethodName = "userPostBuilder")
    private Post(User ownerUser,
                 User author,
                 Instant creationDate,
                 Instant updateDate,
                 Boolean deleted,
                 String text) {
        this.ownerUser = ownerUser;
        this.author = author;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
        this.deleted = deleted;
        this.text = text;
        validateOwner();
    }

    @Builder(builderClassName = "CommunityPostBuilder", builderMethodName = "communityPostBuilder")
    private Post(Community ownerCommunity,
                 User author,
                 Instant creationDate,
                 Instant updateDate,
                 Boolean deleted,
                 String text) {
        this.ownerCommunity = ownerCommunity;
        this.author = author;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
        this.deleted = deleted;
        this.text = text;
        validateOwner();
    }

    @PrePersist @PreUpdate
    private void validateOwner() {
        if ((ownerUser == null) == (ownerCommunity == null)) {
            throw new IllegalStateException(
                    "Post must have exactly one owner: user or community"
            );
        }
    }

}
