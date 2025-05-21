package ru.pocgg.SNSApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_invitations", schema = "SNS")
public class ChatInvitation {

    @EmbeddedId
    private ChatInvitationId id;

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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("chatId")
    @JoinColumn(name = "chatId", referencedColumnName = "id")
    private Chat chat;

    @Column(nullable = false)
    @Size(max = 1000, message = "description max size is 1000")
    private String description;

    @Builder
    public ChatInvitation(User sender,
                          User receiver,
                          Instant creationDate,
                          Chat chat,
                          String description) {
        this.id = ChatInvitationId.builder()
                .senderId(sender.getId())
                .receiverId(receiver.getId())
                .chatId(chat.getId())
                .build();
        this.sender = sender;
        this.receiver = receiver;
        this.creationDate = creationDate;
        this.chat = chat;
        this.description = description;
    }
}
