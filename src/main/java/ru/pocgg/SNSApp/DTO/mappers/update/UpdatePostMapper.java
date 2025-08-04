package ru.pocgg.SNSApp.DTO.mappers.update;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.pocgg.SNSApp.DTO.update.UpdateChatDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePostDTO;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.model.Post;

@Mapper(componentModel = "spring")
public interface UpdatePostMapper {
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDTO(UpdatePostDTO dto, @MappingTarget Post post);

}
