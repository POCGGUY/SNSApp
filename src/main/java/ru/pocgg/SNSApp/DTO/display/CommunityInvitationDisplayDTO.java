package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CommunityInvitationDisplayDTO {
    private Integer senderId;
    private String senderName;
    private Integer receiverId;
    private String receiverName;
    private Integer communityId;
    private String creationDate;
    private String description;
}
