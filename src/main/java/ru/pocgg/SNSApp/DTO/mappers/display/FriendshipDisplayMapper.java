package ru.pocgg.SNSApp.DTO.mappers.display;

import org.mapstruct.Context;
import org.mapstruct.Named;
import ru.pocgg.SNSApp.DTO.display.FriendshipDisplayDTO;
import ru.pocgg.SNSApp.model.Friendship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.pocgg.SNSApp.utils.DateTimeFormatter;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface FriendshipDisplayMapper {

    @Mapping(target = "friendId", expression = "java(friendship.getUser().getId() == currentUserId "
            + "? friendship.getFriend().getId() "
            + ": friendship.getUser().getId())")
    @Mapping(target = "friendName",
            expression = "java(friendship.getUser().getId() == currentUserId "
                    + "? friendship.getFriend().getFirstAndSecondName() "
                    + ": friendship.getUser().getFirstAndSecondName())")
    @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "dateToString")
    FriendshipDisplayDTO toDTO(Friendship friendship, @Context int currentUserId);

    @Named("dateToString")
    static String dateToString(Instant date) {
        return DateTimeFormatter.Format.format(date);
    }
}
