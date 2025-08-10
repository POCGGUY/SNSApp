package ru.pocgg.SNSApp.DTO.mappers.update;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.pocgg.SNSApp.DTO.update.UpdateChatDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateCommunityDTO;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.model.Community;

@Mapper(componentModel = "spring")
public interface UpdateCommunityMapper {
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDTO(UpdateCommunityDTO dto, @MappingTarget Community community);

}
