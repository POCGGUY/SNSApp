package ru.pocgg.SNSApp.DTO.mappers;

import ru.pocgg.SNSApp.model.Community;
import ru.pocgg.SNSApp.DTO.display.CommunityDisplayDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.pocgg.SNSApp.utils.DateTimeFormatter;

import java.time.Instant;

@Mapper(componentModel = "spring" )
public interface CommunityDisplayMapper {

    @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "dateToString")
    CommunityDisplayDTO toDTO(Community community);

    @Named("dateToString")
    static String dateToString(Instant date){
        return DateTimeFormatter.Format.format(date);
    };

}
