package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CommunityFacadeDisplayDTO {
    private CommunityDisplayDTO community;
    private List<PostDisplayDTO> posts;
    private List<CommunityMemberDisplayDTO> members;
}
