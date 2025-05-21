package ru.pocgg.SNSApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications", schema = "SNS")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "receiverId", referencedColumnName = "id")
    private User receiver;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Instant creationDate;

    @Column(nullable = false)
    private Boolean read;

    @Builder
    public Notification(User receiver,
                        String description,
                        Instant creationDate,
                        Boolean read) {
        this.receiver = receiver;
        this.description = description;
        this.creationDate = creationDate;
        this.read = read;
    }
}
