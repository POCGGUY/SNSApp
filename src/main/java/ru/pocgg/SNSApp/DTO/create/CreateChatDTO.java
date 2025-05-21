package ru.pocgg.SNSApp.DTO.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CreateChatDTO {
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;
    @Size(min = 1, max = 1000)
    private String description;
    @NotNull
    private Boolean isPrivate;
}
