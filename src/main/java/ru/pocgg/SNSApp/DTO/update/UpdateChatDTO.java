package ru.pocgg.SNSApp.DTO.update;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UpdateChatDTO {
    @Size(min = 1, max = 100)
    private String name;
    @Size(min = 1, max = 1000)
    private String description;
    private Boolean isPrivate;
}
