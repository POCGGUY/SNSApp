package ru.pocgg.SNSApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "private_messages")
public class PrivateMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "senderId", referencedColumnName = "id")
    private User sender;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "receiverId", referencedColumnName = "id")
    private User receiver;

    @Column(nullable = false)
    private Instant creationDate;

    @Column(nullable = false)
    private Instant updateDate;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(nullable = false)
    @NotBlank
    @Size(min = 1, max = 1000)
    private String text;

    @Builder
    public PrivateMessage(User sender,
                          User receiver,
                          Instant creationDate,
                          Instant updateDate,
                          Boolean deleted,
                          String text) {
        this.sender = sender;
        this.receiver = receiver;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
        this.deleted = deleted;
        this.text = text;
    }
}
