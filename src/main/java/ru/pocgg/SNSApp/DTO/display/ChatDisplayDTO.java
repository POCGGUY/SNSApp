package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ChatDisplayDTO {
    private Integer id;
    private Integer ownerId;
    private String name;
    private String description;
    private String creationDate;
    private Boolean deleted;
    private Boolean isPrivate;
}
