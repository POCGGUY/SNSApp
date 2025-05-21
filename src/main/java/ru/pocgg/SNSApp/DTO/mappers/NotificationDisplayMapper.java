package ru.pocgg.SNSApp.DTO.mappers;

import org.mapstruct.Named;
import ru.pocgg.SNSApp.DTO.display.NotificationDisplayDTO;
import ru.pocgg.SNSApp.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.pocgg.SNSApp.utils.DateTimeFormatter;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface NotificationDisplayMapper {

    @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "dateToString")
    NotificationDisplayDTO toDTO(Notification notification);

    @Named("dateToString")
    static String dateToString(Instant date) {
        return DateTimeFormatter.Format.format(date);
    }
}
