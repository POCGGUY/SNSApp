package ru.pocgg.SNSApp.DTO.mappers.display;

import ru.pocgg.SNSApp.model.CommunityMember;
import ru.pocgg.SNSApp.DTO.display.CommunityMemberDisplayDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.pocgg.SNSApp.utils.DateTimeFormatter;
import java.time.Instant;

@Mapper(componentModel = "spring")
public interface CommunityMemberDisplayMapper {

    @Mapping(source = "community.id", target = "communityId")
    @Mapping(source = "member.id", target = "memberId")
    @Mapping(target = "memberName",
            expression = "java(communityMember.getMember().getFirstAndSecondName())")
    @Mapping(source = "entryDate", target = "entryDate", qualifiedByName = "dateToString")
    CommunityMemberDisplayDTO toDTO(CommunityMember communityMember);

    @Named("dateToString")
    static String dateToString(Instant date) {
        return DateTimeFormatter.Format.format(date);
    }
}
