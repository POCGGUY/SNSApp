package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.Gender;
import org.hibernate.Session;

import java.util.List;

public interface UserServiceDAO {
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(User user);
    User getUserById(int id);
    User getUserByUserName(String userName);
    User getUserByEmail(String email);
    List<User> getAllUsers();
    List<User> searchUsers(String firstName, String secondName, Integer age, Gender gender);
    void forceFlush();
}
