package ru.pocgg.SNSApp.DTO.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UpdateUserPasswordDTO {
    @NotBlank
    @Size(min = 5, max = 30)
    private String oldPassword;
    @NotBlank
    @Size(min = 5, max = 30)
    private String newPassword;
}
