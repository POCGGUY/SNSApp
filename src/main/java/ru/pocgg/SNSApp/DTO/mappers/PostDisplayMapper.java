package ru.pocgg.SNSApp.DTO.mappers;

import org.mapstruct.Named;
import ru.pocgg.SNSApp.DTO.display.PostDisplayDTO;
import ru.pocgg.SNSApp.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.pocgg.SNSApp.utils.DateTimeFormatter;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface PostDisplayMapper {

    @Mapping(target = "ownerName", expression = "java(getOwnerName(post))")
    @Mapping(source = "ownerCommunity.id", target = "ownerCommunityId")
    @Mapping(source = "ownerUser.id", target = "ownerUserId")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(target = "authorName", expression = "java(post.getAuthor().getFirstAndSecondName())")
    @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "dateToString")
    @Mapping(source = "updateDate", target = "updateDate", qualifiedByName = "updateDateToString")
    PostDisplayDTO toDTO(Post post);

    default String getOwnerName(Post post) {
        if (post.getOwnerUser() != null) {
            return post.getOwnerUser().getFirstAndSecondName();
        } else {
            return post.getOwnerCommunity().getCommunityName();
        }
    }

    @Named("updateDateToString")
    static String updateDateToString(Instant date) {
        if (date != null) {
            return DateTimeFormatter.Format.format(date);
        } else {
            return null;
        }
    }

    @Named("dateToString")
    static String dateToString(Instant date) {
        return DateTimeFormatter.Format.format(date);
    }
}
