package ru.pocgg.SNSApp.DTO.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UpdatePostCommentDTO {
    @NotBlank
    @Size(min = 1, max = 1000)
    private String text;
}
