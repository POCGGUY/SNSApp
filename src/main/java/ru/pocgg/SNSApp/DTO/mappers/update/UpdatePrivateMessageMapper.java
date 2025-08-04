package ru.pocgg.SNSApp.DTO.mappers.update;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.pocgg.SNSApp.DTO.update.UpdateChatDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePrivateMessageDTO;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.model.PrivateMessage;

@Mapper(componentModel = "spring")
public interface UpdatePrivateMessageMapper {
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDTO(UpdatePrivateMessageDTO dto, @MappingTarget PrivateMessage privateMessage);

}
