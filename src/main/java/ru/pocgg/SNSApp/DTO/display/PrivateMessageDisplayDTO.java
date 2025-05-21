package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PrivateMessageDisplayDTO {
    private Integer id;
    private Integer senderId;
    private String senderName;
    private String creationDate;
    private String updateDate;
    private Boolean deleted;
    private String text;
}
