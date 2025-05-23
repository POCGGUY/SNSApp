package ru.pocgg.SNSApp.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.pocgg.SNSApp.DTO.create.CreateUserDTO;
import ru.pocgg.SNSApp.DTO.create.CreateUserWithRoleDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateUserDTO;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.SystemRole;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.events.events.UserDeactivatedEvent;
import ru.pocgg.SNSApp.model.exceptions.FoundUniqueExistingValuesException;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.UserServiceDAO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService extends TemplateService{
    private final UserServiceDAO userServiceDAO;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    private final SystemRole defaultSystemRole = SystemRole.USER;
    private final boolean defaultPostsPublic = true;
    private final boolean defaultAcceptingPrivateMsgs = true;

    public User getUserById(int userId) {
        return getUserByIdOrThrowException(userId);
    }

    public User getUserByUserName(String userName) {
        return getUserByUserNameOrThrowException(userName);
    }

    public User createUser(CreateUserDTO createUserDTO) {
        validate(createUserDTO.getUserName(), createUserDTO.getEmail());
        User user = User.builder()
                .userName(createUserDTO.getUserName())
                .creationDate(Instant.now())
                .birthDate(LocalDate.parse(createUserDTO.getBirthDate()))
                .password(passwordEncoder.encode(createUserDTO.getPassword()))
                .email(createUserDTO.getEmail())
                .firstName(createUserDTO.getFirstName())
                .secondName(createUserDTO.getSecondName())
                .thirdName(createUserDTO.getThirdName())
                .gender(Gender.fromString(createUserDTO.getGender()))
                .systemRole(defaultSystemRole)
                .description(createUserDTO.getDescription())
                .deleted(false)
                .postsPublic(defaultPostsPublic)
                .acceptingPrivateMsgs(defaultAcceptingPrivateMsgs)
                .banned(false).build();
        userServiceDAO.addUser(user);
        userServiceDAO.forceFlush();
        logger.info("created user with name {}, id: {}", user.getUserName(),  user.getId());
        return user;
    }

    public User createUserWithSystemRole(CreateUserWithRoleDTO dto) {
        validate(dto.getUserName(), dto.getEmail());
        User user = User.builder()
                .userName(dto.getUserName())
                .creationDate(Instant.now())
                .birthDate(LocalDate.parse(dto.getBirthDate()))
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .secondName(dto.getSecondName())
                .thirdName(dto.getThirdName())
                .gender(Gender.fromString(dto.getGender()))
                .systemRole(SystemRole.fromString(dto.getSystemRole()))
                .description(dto.getDescription())
                .deleted(false)
                .postsPublic(defaultPostsPublic)
                .acceptingPrivateMsgs(defaultAcceptingPrivateMsgs)
                .banned(false).build();
        userServiceDAO.addUser(user);
        userServiceDAO.forceFlush();
        logger.info("created user with name {}, id: {}, and system role: {}",
                user.getUserName(),  user.getId(), user.getSystemRole().toString());
        return user;
    }

    public List<User> searchUsers(String firstName,
                                  String secondName,
                                  Integer age,
                                  Gender gender) {
        return userServiceDAO.searchUsers(firstName, secondName, age, gender);
    }

    public void updateUser(int userId, UpdateUserDTO dto) {
        User user = getUserByIdOrThrowException(userId);
        updateBirthDate(user, dto.getBirthDate());
        updateEmail(user, dto.getEmail());
        updateFirstName(user, dto.getFirstName());
        updateSecondName(user, dto.getSecondName());
        updateThirdName(user, dto.getThirdName());
        updateGender(user, dto.getGender());
        updateDescription(user, dto.getDescription());
        updateAcceptingPrivateMsgs(user, dto.getAcceptingPrivateMsgs());
        updatePostsPublic(user, dto.getPostsPublic());
    }

    public void setSystemRole(int userId, SystemRole systemRole) {
        User user = getUserByIdOrThrowException(userId);
        if (user.getSystemRole() == systemRole) {
            logger.info("user with id: {} is already has systemRole: {}", userId, systemRole.toString());
        } else {
            user.setSystemRole(systemRole);
            logger.info("user with id: {} now have systemRole: {}", userId, systemRole.toString());
        }
    }

    public void setIsBanned(int userId, boolean value) {
        User user = getUserByIdOrThrowException(userId);
        if (user.getBanned() == value) {
            logger.info("user with id: {} is already has banned: {}", userId, value);
        } else {
            user.setBanned(value);
            if(value){
                eventPublisher.publishEvent(new UserDeactivatedEvent(userId));
            }
            logger.info("user with id: {} now have banned: {}", userId, value);
        }
    }

    public void setIsDeleted(int userId, boolean value) {
        User user = getUserByIdOrThrowException(userId);
        if (user.getDeleted() == value) {
            logger.info("user with id: {} is already has deleted: {}", userId, value);
        } else {
            user.setDeleted(value);
            if(value){
                eventPublisher.publishEvent(new UserDeactivatedEvent(userId));
            }
            logger.info("user with id: {} now have deleted: {}", userId, value);
        }
    }

    private void updateBirthDate(User user, String birthDate) {
        if (birthDate != null) {
            user.setBirthDate(LocalDate.parse(birthDate));
            logger.info("User with id: {} has updated birth date to: {}", user.getId(), birthDate);
        }
    }
    private void updateEmail(User user, String email) {
        if (email != null && !email.equals(user.getEmail())) {
            validateByEmail(email);
            user.setEmail(email);
            logger.info("User with id: {} has updated email: {}", user.getId(), email);
        }
    }
    private void updateFirstName(User user, String firstName) {
        if (firstName != null) {
            user.setFirstName(firstName);
            logger.info("User with id: {} has updated first name: {}", user.getId(), firstName);
        }
    }
    private void updateSecondName(User user, String secondName) {
        if (secondName != null) {
            user.setSecondName(secondName);
            logger.info("User with id: {} has updated second name: {}", user.getId(), secondName);
        }
    }
    private void updateThirdName(User user, String thirdName) {
        if (thirdName != null) {
            user.setThirdName(thirdName);
            logger.info("User with id: {} has updated third name: {}", user.getId(), thirdName);
        }
    }
    private void updateGender(User user, String gender) {
        if (gender != null) {
            user.setGender(Gender.fromString(gender));
            logger.info("User with id: {} has updated gender to: {}", user.getId(), gender);
        }
    }
    private void updateDescription(User user, String description) {
        if (description != null) {
            user.setDescription(description);
            logger.info("User with id: {} has updated description: {}", user.getId(), description);
        }
    }
    private void updateAcceptingPrivateMsgs(User user, Boolean value) {
        if (value != null) {
            user.setAcceptingPrivateMsgs(value);
            logger.info("User with id: {} has updated flag acceptingPrivateMsgs to: {}", user.getId(), value);
        }
    }
    private void updatePostsPublic(User user, Boolean value) {
        if (value != null) {
            user.setPostsPublic(value);
            logger.info("User with id: {} has updated flag postsPublic to: {}", user.getId(), value);
        }
    }
    private User getUserByIdOrThrowException(int Id) {
        User user = userServiceDAO.getUserById(Id);
        if (user != null) {
            return user;
        } else {
            throw new EntityNotFoundException("User with id: " + Id + " not found");
        }
    }

    private User getUserByEmailOrThrowException(String email) {
        User user = userServiceDAO.getUserByEmail(email);
        if (user != null) {
            return user;
        } else {
            throw new EntityNotFoundException("User with email: " + email + " not found");
        }
    }

    private User getUserByUserNameOrThrowException(String userName) {
        User user = userServiceDAO.getUserByUserName(userName);
        if (user != null) {
            return user;
        } else {
            throw new EntityNotFoundException("User with username: " + userName + " not found");
        }
    }

    private void validate(String userName, String email) {
        validateByUserName(userName);
        validateByEmail(email);
    }

    private void validateByUserName(String userName) {
        if(userServiceDAO.getUserByUserName(userName) != null) {
            throw new FoundUniqueExistingValuesException("user with userName: " + userName + " already exists");
        }
    }

    private void validateByEmail(String email) {
        if(userServiceDAO.getUserByEmail(email) != null) {
            throw new FoundUniqueExistingValuesException("user with email: " + email + " already exists");
        }
    }
}
