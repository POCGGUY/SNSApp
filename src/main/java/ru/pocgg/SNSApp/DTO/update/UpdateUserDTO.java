package ru.pocgg.SNSApp.DTO.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.annotations.validations.EnumValue;
import ru.pocgg.SNSApp.model.annotations.validations.StringValidBirthDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UpdateUserDTO {
    @StringValidBirthDate
    private String birthDate;
    @Size(min = 1, max = 50)
    private String firstName;
    @Size(min = 1, max = 50)
    private String secondName;
    @Size(min = 1, max = 50)
    private String thirdName;
    @Size(min = 5, max = 30)
    private String password;
    @Email
    private String email;
    @EnumValue(enumClass = Gender.class, message = "value must be either MALE or FEMALE")
    private String gender;
    @Size(min = 1, max = 1000)
    private String description;
    private Boolean postsPublic;
    private Boolean acceptingPrivateMsgs;
}
