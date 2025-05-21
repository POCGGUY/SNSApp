package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ChatMessageDisplayDTO {
    private Integer id;
    private Integer chatId;
    private Integer senderId;
    private String senderName;
    private String updateDate;
    private Boolean deleted;
    private String sendingDate;
    private String text;
}
