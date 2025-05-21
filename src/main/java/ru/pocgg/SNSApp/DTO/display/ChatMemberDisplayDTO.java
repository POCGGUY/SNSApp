package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ChatMemberDisplayDTO {
    private Integer chatId;
    private Integer memberId;
    private String memberName;
    private String entryDate;
}
