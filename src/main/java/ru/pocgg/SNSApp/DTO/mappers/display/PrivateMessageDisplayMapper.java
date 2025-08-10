package ru.pocgg.SNSApp.DTO.mappers.display;

import org.mapstruct.Named;
import ru.pocgg.SNSApp.DTO.display.PrivateMessageDisplayDTO;
import ru.pocgg.SNSApp.model.PrivateMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.pocgg.SNSApp.utils.DateTimeFormatter;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface PrivateMessageDisplayMapper {
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(target = "senderName", expression = "java(message.getSender().getFirstAndSecondName())")
    @Mapping(source = "updateDate", target = "updateDate", qualifiedByName = "updateDateToString")
    @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "dateToString")
    PrivateMessageDisplayDTO toDTO(PrivateMessage message);

    @Named("dateToString")
    static String dateToString(Instant date) {
        return DateTimeFormatter.Format.format(date);
    }

    @Named("updateDateToString")
    static String updateDateToString(Instant date) {
        if (date != null) {
            return DateTimeFormatter.Format.format(date);
        } else {
            return null;
        }
    }
}
