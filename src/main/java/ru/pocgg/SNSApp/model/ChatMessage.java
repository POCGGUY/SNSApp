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
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "chatId", referencedColumnName = "id")
    private Chat chat;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "senderId", referencedColumnName = "id")
    private User sender;

    @Column(nullable = false)
    private Instant updateDate;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(nullable = false)
    private Instant sendingDate;

    @Column(nullable = false)
    @NotBlank(message = "message text should not be empty")
    @Size(min = 1, max = 1000)
    private String text;

    @Builder
    public ChatMessage(User sender,
                       Chat chat,
                       Instant updateDate,
                       boolean deleted,
                       Instant sendingDate,
                       String text) {
        this.sender = sender;
        this.chat = chat;
        this.updateDate = updateDate;
        this.deleted = deleted;
        this.sendingDate = sendingDate;
        this.text = text;
    }
}
