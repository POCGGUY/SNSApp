package ru.pocgg.SNSApp.DTO.mappers.update;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.pocgg.SNSApp.DTO.update.UpdateChatDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateUserPasswordDTO;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.model.User;

@Mapper(componentModel = "spring")
public interface UpdateUserPasswordMapper {
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDTO(UpdateUserPasswordDTO dto, @MappingTarget User user);

}
