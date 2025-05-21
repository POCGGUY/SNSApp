package ru.pocgg.SNSApp.DTO.mappers;

import ru.pocgg.SNSApp.model.ChatMember;
import ru.pocgg.SNSApp.DTO.display.ChatMemberDisplayDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.pocgg.SNSApp.utils.DateTimeFormatter;
import java.time.Instant;

@Mapper(componentModel = "spring")
public interface ChatMemberDisplayMapper {

    @Mapping(source = "chat.id", target = "chatId")
    @Mapping(source = "member.id", target = "memberId")
    @Mapping(target = "memberName", expression = "java(chatMember.getMember().getFirstAndSecondName())")
    @Mapping(source = "entryDate", target = "entryDate", qualifiedByName = "dateToString")
    ChatMemberDisplayDTO toDTO(ChatMember chatMember);

    @Named("dateToString")
    static String dateToString(Instant date) {
        return DateTimeFormatter.Format.format(date);
    }
}
