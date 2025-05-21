package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ChatInvitationDisplayDTO {
    private Integer senderId;
    private Integer receiverId;
    private Integer chatId;
    private String senderName;
    private String creationDate;
    private String chatName;
    private String description;
}
