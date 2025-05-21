package ru.pocgg.SNSApp.DTO.mappers;

import org.mapstruct.Named;
import ru.pocgg.SNSApp.model.FriendshipRequest;
import ru.pocgg.SNSApp.DTO.display.FriendshipRequestDisplayDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.pocgg.SNSApp.utils.DateTimeFormatter;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface FriendshipRequestDisplayMapper {

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(target = "senderName",
            expression = "java(friendshipRequest.getSender().getFirstAndSecondName())")
    @Mapping(target = "receiverName",
            expression = "java(friendshipRequest.getReceiver().getFirstAndSecondName())")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "dateToString")
    FriendshipRequestDisplayDTO toDTO(FriendshipRequest friendshipRequest);

    @Named("dateToString")
    static String dateToString(Instant date) {
        return DateTimeFormatter.Format.format(date);
    }
}
