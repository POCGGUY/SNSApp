package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.CommunityInvitation;
import ru.pocgg.SNSApp.model.CommunityInvitationId;

import java.util.List;

public interface CommunityInvitationServiceDAO {
    CommunityInvitation getInvitationById(CommunityInvitationId id);
    List<CommunityInvitation> getInvitationsBySenderId(int senderId);
    List<CommunityInvitation> getInvitationsByReceiverId(int receiverId);
    List<CommunityInvitation> getInvitationsByCommunityId(int communityId);
    List<CommunityInvitation> getInvitationsByReceiverAndCommunityId(int receiverId, int communityId);
    List<CommunityInvitation> getAllInvitations();
    void removeBySenderId(int senderId);
    void removeByCommunityId(int communityId);
    void removeBySenderAndCommunity(int senderId, int communityId);
    void removeByReceiverId(int receiverId);
    void addInvitation(CommunityInvitation invitation);
    void removeInvitation(CommunityInvitation invitation);
    void forceFlush();
}
