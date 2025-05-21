package ru.pocgg.SNSApp.DTO.display;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PostFacadeDTO {
    private PostDisplayDTO post;
    private List<PostCommentDisplayDTO> postComments;
}
