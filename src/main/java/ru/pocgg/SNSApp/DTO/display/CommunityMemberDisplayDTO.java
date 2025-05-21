package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CommunityMemberDisplayDTO {
    private Integer communityId;
    private Integer memberId;
    private String memberName;
    private String entryDate;
    private String memberRole;
}
