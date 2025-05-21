package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PostDisplayDTO {
    private Integer id;
    private Integer ownerCommunityId;
    private Integer ownerUserId;
    private String ownerName;
    private Integer authorId;
    private String authorName;
    private String creationDate;
    private String updateDate;
    private Boolean deleted;
    private String text;
}
