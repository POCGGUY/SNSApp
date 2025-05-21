package ru.pocgg.SNSApp.DTO.mappers;

import ru.pocgg.SNSApp.model.ChatMessage;
import ru.pocgg.SNSApp.DTO.display.ChatMessageDisplayDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.time.Instant;

import ru.pocgg.SNSApp.utils.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface ChatMessageDisplayMapper {

    @Mapping(source = "chat.id", target = "chatId")
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(target = "senderName", expression = "java(chatMessage.getSender().getFirstAndSecondName())")
    @Mapping(source = "updateDate", target = "updateDate", qualifiedByName = "updateDateToString")
    @Mapping(source = "sendingDate", target = "sendingDate", qualifiedByName = "dateToString")
    ChatMessageDisplayDTO toDTO(ChatMessage chatMessage);

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
