package ru.pocgg.SNSApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "chats_members", schema = "SNS")
public class ChatMember {

    @EmbeddedId
    private ChatMemberId id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("chatId")
    @JoinColumn(name = "chatId", referencedColumnName = "id")
    private Chat chat;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @MapsId("memberId")
    @JoinColumn(name = "memberId", referencedColumnName = "id")
    private User member;

    @Column(nullable = false)
    private Instant entryDate;

    @Builder
    public ChatMember(Chat chat,
                      User member,
                      Instant entryDate) {
        this.id = ChatMemberId.builder()
                .chatId(chat.getId())
                .memberId(member.getId())
                .build();
        this.chat = chat;
        this.member = member;
        this.entryDate = entryDate;
    }
}
