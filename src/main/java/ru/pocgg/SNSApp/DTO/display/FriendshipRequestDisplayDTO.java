package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class FriendshipRequestDisplayDTO {
    private Integer senderId;
    private String senderName;
    private String receiverName;
    private Integer receiverId;
    private String creationDate;
}
