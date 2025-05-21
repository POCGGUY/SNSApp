package ru.pocgg.SNSApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts_comments")
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", referencedColumnName = "id", nullable = false)
    private Post post;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "authorId", referencedColumnName = "id", nullable = false)
    private User author;

    @Column(nullable = false)
    private Instant creationDate;

    @Column(nullable = false)
    private Instant updateDate;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(nullable = false)
    @NotBlank
    @Size(min = 1, max = 1000)
    private String text;

    @Builder
    public PostComment(Post post,
                       User author,
                       Instant creationDate,
                       Instant updateDate,
                       String text,
                       Boolean deleted) {
        this.post = post;
        this.author = author;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
        this.text = text;
        this.deleted = deleted;
    }
}
