package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.model.ChatMember;
import ru.pocgg.SNSApp.model.ChatMemberId;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatMemberServiceDAO;
import ru.pocgg.SNSApp.services.DAO.requests.ChatMemberServiceDAORequests;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Scope("singleton")
public class ChatMemberServiceDAOImpl implements ChatMemberServiceDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public ChatMember getChatMemberById(ChatMemberId id) {
        try {
            return getSession()
                    .createQuery(ChatMemberServiceDAORequests.GET_BY_ID, ChatMember.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<ChatMember> getAllChatMembers() {
        return getSession()
                .createQuery(ChatMemberServiceDAORequests.GET_ALL, ChatMember.class)
                .getResultList();
    }

    public List<ChatMember> getMembersByChatId(int chatId) {
        return getSession()
                .createQuery(ChatMemberServiceDAORequests.GET_BY_CHAT_ID, ChatMember.class)
                .setParameter("chatId", chatId)
                .getResultList();
    }

    public List<Chat> getChatsByMemberId(int memberId) {
        return getSession()
                .createQuery(ChatMemberServiceDAORequests.GET_CHATS_BY_MEMBER_ID, Chat.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    public void addChatMember(ChatMember chatMember) {
        getSession().persist(chatMember);
    }

    public void updateChatMember(ChatMember chatMember) {
        getSession().merge(chatMember);
    }

    public void removeChatMember(ChatMember chatMember) {
        getSession().remove(chatMember);
    }

    public void forceFlush() {
        getSession().flush();
    }
}
