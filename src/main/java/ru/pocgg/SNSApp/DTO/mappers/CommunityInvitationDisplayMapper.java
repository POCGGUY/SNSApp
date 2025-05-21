package ru.pocgg.SNSApp.DTO.mappers;

import ru.pocgg.SNSApp.model.CommunityInvitation;
import ru.pocgg.SNSApp.DTO.display.CommunityInvitationDisplayDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.pocgg.SNSApp.utils.DateTimeFormatter;
import java.time.Instant;

@Mapper(componentModel = "spring")
public interface CommunityInvitationDisplayMapper {
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(target = "senderName",
            expression = "java(communityInvitation.getSender().getFirstAndSecondName())")
    @Mapping(target = "receiverName",
            expression = "java(communityInvitation.getReceiver().getFirstAndSecondName())")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "community.id", target = "communityId")
    @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "dateToString")
    CommunityInvitationDisplayDTO toDTO(CommunityInvitation communityInvitation);

    @Named("dateToString")
    static String dateToString(Instant date) {
        return DateTimeFormatter.Format.format(date);
    }
}
