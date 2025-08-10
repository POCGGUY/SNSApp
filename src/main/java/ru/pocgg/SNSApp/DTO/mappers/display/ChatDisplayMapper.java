package ru.pocgg.SNSApp.DTO.mappers.display;

import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.DTO.display.ChatDisplayDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.pocgg.SNSApp.utils.DateTimeFormatter;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface ChatDisplayMapper {

    @Mapping(source = "private", target = "isPrivate")
    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "dateToString")
    ChatDisplayDTO toDTO(Chat chat);

    @Named("dateToString")
    static String dateToString(Instant date) {
        return DateTimeFormatter.Format.format(date);
    }
}
