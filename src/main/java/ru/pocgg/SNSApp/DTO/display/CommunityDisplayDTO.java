package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CommunityDisplayDTO {
    private Integer id;
    private String communityName;
    private String creationDate;
    private String description;
    private Boolean isPrivate;
    private Boolean deleted;
    private Boolean banned;
}
