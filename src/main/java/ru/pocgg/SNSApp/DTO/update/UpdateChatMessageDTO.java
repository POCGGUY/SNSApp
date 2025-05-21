package ru.pocgg.SNSApp.DTO.update;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UpdateChatMessageDTO {
    private String text;
}
