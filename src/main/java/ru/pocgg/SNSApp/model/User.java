package ru.pocgg.SNSApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.pocgg.SNSApp.model.annotations.validations.ValidBirthDate;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "username should not be empty")
    @Size(min = 3, max = 20)
    private String userName;

    @Column(nullable = false)
    @NotNull(message = "creation date should not be empty")
    private Instant creationDate;

    @Column(nullable = false)
    @NotNull(message = "birth date should not be empty")
    @ValidBirthDate
    private LocalDate birthDate;

    @Column(nullable = false)
    @NotBlank(message = "first name should not be empty")
    @Size(min = 1, max = 50)
    private String firstName;

    @Column(nullable = false)
    @NotBlank(message = "second name should not be empty")
    @Size(min = 1, max = 50)
    private String secondName;

    @Column(nullable = false)
    @NotBlank(message = "password should not be empty")
    @Size(min = 1, max = 1000)
    private String password;

    @Column(nullable = false)
    @Email
    private String email;

    @Column(nullable = true)
    @Size(min = 1, max = 50)
    private String thirdName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SystemRole systemRole;

    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(nullable = false)
    private Boolean acceptingPrivateMsgs;

    @Column(nullable = false)
    private Boolean postsPublic;

    @Column(nullable = false)
    private Boolean banned;

    @Builder
    public User(String userName, Instant creationDate, LocalDate birthDate, String password,
                String email, String firstName, String secondName, String thirdName,
                Gender gender, SystemRole systemRole, String description,
                Boolean deleted, Boolean acceptingPrivateMsgs,
                Boolean postsPublic, Boolean banned) {
        this.userName = userName;
        this.creationDate = creationDate;
        this.birthDate = birthDate;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.secondName = secondName;
        this.thirdName = thirdName;
        this.gender = gender;
        this.systemRole = systemRole;
        this.description = description;
        this.deleted = deleted;
        this.acceptingPrivateMsgs = acceptingPrivateMsgs;
        this.postsPublic = postsPublic;
        this.banned = banned;
    }

    public String getFirstAndSecondName() {
        return firstName + " " + secondName;
    }

    public Boolean isModerator(){
        return systemRole == SystemRole.MODERATOR || systemRole == SystemRole.ADMIN;
    }

}
