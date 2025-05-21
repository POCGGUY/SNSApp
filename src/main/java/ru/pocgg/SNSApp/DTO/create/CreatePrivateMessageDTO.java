package ru.pocgg.SNSApp.DTO.create;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CreatePrivateMessageDTO {
    @NotBlank
    private String text;
}
