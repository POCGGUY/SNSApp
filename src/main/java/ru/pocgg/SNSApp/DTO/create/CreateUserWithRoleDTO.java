package ru.pocgg.SNSApp.DTO.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.SystemRole;
import ru.pocgg.SNSApp.model.annotations.validations.EnumValue;
import ru.pocgg.SNSApp.model.annotations.validations.StringValidBirthDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CreateUserWithRoleDTO {
    @NotBlank
    @Size(min = 5, max = 20)
    private String userName;
    @NotBlank
    @StringValidBirthDate
    private String birthDate;
    @NotBlank
    @Size(min = 1, max = 50)
    private String firstName;
    @NotBlank
    @Size(min = 1, max = 50)
    private String secondName;
    @Size(min = 1, max = 50)
    private String thirdName;
    @NotBlank
    @Size(min = 5, max = 30)
    private String password;
    @NotBlank
    @Email
    private String email;
    @NotNull
    @EnumValue(enumClass = Gender.class, message = "value must be either MALE or FEMALE")
    private String gender;
    @Size(min = 1, max = 1000)
    private String description;
    @NotNull
    @EnumValue(enumClass = SystemRole.class, message = "SystemRole must be either ADMIN or MODERATOR or USER")
    private String systemRole;
}
