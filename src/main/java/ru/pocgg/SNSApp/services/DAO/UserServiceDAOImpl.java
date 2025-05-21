package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.services.DAO.interfaces.UserServiceDAO;
import ru.pocgg.SNSApp.services.DAO.requests.UserServiceDAORequests;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@Scope("singleton")
public class UserServiceDAOImpl implements UserServiceDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession(){
        return entityManager.unwrap(Session.class);
    }

    public void addUser(User user){
        getSession().persist(user);
    }

    public void updateUser(User user){
        getSession().merge(user);
    }

    public void deleteUser(User user){
        getSession().remove(user);
    }

    public User getUserById(int id){
        try {
            return getSession().get(User.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    public User getUserByEmail(String email){
        try {
            return getSession().createQuery(UserServiceDAORequests.GET_BY_EMAIL, User.class)
                    .setParameter("email", email).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User getUserByUserName(String userName){
        try {
            return getSession().createQuery(UserServiceDAORequests.GET_BY_USER_NAME, User.class)
                    .setParameter("userName", userName).getSingleResult();
        } catch (NoResultException e){
            return null;
        }
    }

    public List<User> getAllUsers(){
        try {
            return getSession().createQuery(UserServiceDAORequests.GET_ALL, User.class).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<User> searchUsers(String firstName,
                                  String secondName,
                                  Integer age,
                                  Gender gender) {
        try {
            CriteriaBuilder criteriaBuilder = getSession().getCriteriaBuilder();
            CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
            Root<User> usersRoot = criteriaQuery.from(User.class);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isFalse(usersRoot.get("deleted")));
            predicates.add(criteriaBuilder.isFalse(usersRoot.get("banned")));

            if (firstName != null && !firstName.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(usersRoot.get("firstName")),
                        "%" + firstName.toLowerCase() + "%"
                ));
            }
            if (secondName != null && !secondName.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(usersRoot.get("secondName")),
                        "%" + secondName.toLowerCase() + "%"
                ));
            }
            if (gender != null) {
                predicates.add(criteriaBuilder.equal(usersRoot.get("gender"), gender));
            }
            if (age != null) {
                LocalDate today = LocalDate.now();
                LocalDate bornAfter = today.minusYears(age + 1).plusDays(1);
                LocalDate bornBefore = today.minusYears(age);
                predicates.add(criteriaBuilder.between(usersRoot.get("birthDate"), bornAfter, bornBefore));
            }

            criteriaQuery.where(predicates.toArray(new Predicate[0]));
            return getSession().createQuery(criteriaQuery).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void forceFlush(){
        getSession().flush();
    }


}
