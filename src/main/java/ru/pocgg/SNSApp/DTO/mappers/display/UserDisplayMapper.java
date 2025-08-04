package ru.pocgg.SNSApp.DTO.mappers.display;

import ru.pocgg.SNSApp.DTO.display.UserDisplayDTO;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.SystemRole;
import ru.pocgg.SNSApp.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.pocgg.SNSApp.model.exceptions.InvalidDateFormat;
import ru.pocgg.SNSApp.utils.DateTimeFormatter;

import java.time.Instant;
import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface UserDisplayMapper {

    @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "dateToString")
    @Mapping(source = "birthDate", target = "birthDate", qualifiedByName = "birthDateToString")
    @Mapping(source = "gender", target = "gender", qualifiedByName = "genderToString")
    UserDisplayDTO toDTO(User user);

    @Named("genderToString")
    static String genderToString(Gender gender) {
        return gender.toString();
    }

    @Named("dateToString")
    static String dateToString(Instant date){
        return DateTimeFormatter.Format.format(date);
    };

    @Named("birthDateToString")
    static String birthDateToString(LocalDate birthDate){
        try {
            return DateTimeFormatter.birthDate.format(birthDate);
        } catch (Exception e) {
            throw new InvalidDateFormat("Invalid birth date");
        }
    }

}
