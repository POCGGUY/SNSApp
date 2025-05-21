package ru.pocgg.SNSApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;


@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chats", schema = "SNS")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "ownerId", referencedColumnName = "id")
    private User owner;

    @Column(nullable = true)
    @Size(max = 1000)
    private String description;

    @Column(nullable = false)
    @Size(min = 1, max = 100)
    private String name;

    @Column(nullable = false)
    private Instant creationDate;

    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false)
    private boolean isPrivate;

    @Builder
    public Chat(User owner,
                String description,
                Instant creationDate,
                String name,
                boolean deleted,
                boolean isPrivate) {
        this.owner = owner;
        this.description = description;
        this.name = name;
        this.creationDate = creationDate;
        this.deleted = deleted;
        this.isPrivate = isPrivate;
    }
}
