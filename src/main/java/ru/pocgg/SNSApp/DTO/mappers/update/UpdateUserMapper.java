package ru.pocgg.SNSApp.DTO.mappers.update;

import org.mapstruct.*;
import ru.pocgg.SNSApp.DTO.update.UpdateChatDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateUserDTO;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.User;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface UpdateUserMapper {

    @Mapping(source = "gender", target = "gender", qualifiedByName = "asGender")
    @Mapping(source = "birthDate", target = "birthDate", qualifiedByName = "asLocalDate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDTO(UpdateUserDTO dto, @MappingTarget User user);

    @Named("asLocalDate")
    static LocalDate asLocalDate(String date) {
        return LocalDate.parse(date);
    }

    @Named("asGender")
    static Gender asGender(String gender) {
        return Gender.fromString(gender);
    }
}
