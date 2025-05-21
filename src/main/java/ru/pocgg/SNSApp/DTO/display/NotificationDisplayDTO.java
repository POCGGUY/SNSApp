package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class NotificationDisplayDTO {
    private Integer id;
    private String description;
    private String creationDate;
    private Boolean read;
}
