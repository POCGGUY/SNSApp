package ru.pocgg.SNSApp.DTO.mappers;

import org.mapstruct.*;
import ru.pocgg.SNSApp.model.ChatInvitation;
import ru.pocgg.SNSApp.DTO.display.ChatInvitationDisplayDTO;
import ru.pocgg.SNSApp.utils.DateTimeFormatter;
import java.time.Instant;

@Mapper(componentModel = "spring")
public interface ChatInvitationDisplayMapper {

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "chat.id", target = "chatId")
    @Mapping(target = "senderName", expression = "java(chatInvitation.getSender().getFirstAndSecondName())")
    @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "dateToString")
    ChatInvitationDisplayDTO toDTO(ChatInvitation chatInvitation);

    @Named("dateToString")
    static String dateToString(Instant date) {
        return DateTimeFormatter.Format.format(date);
    }
}
