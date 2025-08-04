package ru.pocgg.SNSApp.DTO.mappers.display;

import org.mapstruct.Named;
import ru.pocgg.SNSApp.DTO.display.PostCommentDisplayDTO;
import ru.pocgg.SNSApp.model.PostComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.pocgg.SNSApp.utils.DateTimeFormatter;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface PostCommentDisplayMapper {

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(target = "authorName", expression = "java(postComment.getAuthor().getFirstAndSecondName())")
    @Mapping(source = "updateDate", target = "updateDate", qualifiedByName = "updateDateToString")
    @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "dateToString")
    PostCommentDisplayDTO toDTO(PostComment postComment);

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
