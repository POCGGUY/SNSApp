package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UserDisplayDTO {
    private Integer id;
    private String creationDate;
    private String birthDate;
    private String firstName;
    private String secondName;
    private String thirdName;
    private String gender;
    private String description;
    private Boolean deleted;
    private Boolean acceptingPrivateMsgs;
    private Boolean postsPublic;
    private Boolean banned;
}
