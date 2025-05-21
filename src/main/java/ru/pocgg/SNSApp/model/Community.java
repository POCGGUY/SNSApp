package ru.pocgg.SNSApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "communities")
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ownerId", referencedColumnName = "id")
    private User owner;

    @Column(nullable = false, length = 30)
    @NotBlank(message = "community name should not be empty")
    @Size(min = 1, max = 30)
    private String communityName;

    @Column(nullable = false)
    @NotNull(message = "creation date should not be empty")
    private Instant creationDate;

    @Column(nullable = true)
    @Size(min = 1)
    private String description;

    @Column(nullable = false)
    private Boolean isPrivate;

    @Column(nullable = false)
    private Boolean banned;

    @Column(nullable = false)
    private Boolean deleted;

    @Builder
    public Community(
            User owner,
            String communityName,
            Instant creationDate,
            String description,
            Boolean isPrivate,
            Boolean deleted,
            Boolean banned) {
        this.owner = owner;
        this.communityName = communityName;
        this.creationDate = creationDate;
        this.isPrivate = isPrivate;
        this.description = description;
        this.deleted = deleted;
        this.banned = banned;
    }
}

