package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UserFacadeDisplayDTO {
    private UserDisplayDTO user;
    private List<PostDisplayDTO> posts;
    private List<FriendshipDisplayDTO> friends;
}
