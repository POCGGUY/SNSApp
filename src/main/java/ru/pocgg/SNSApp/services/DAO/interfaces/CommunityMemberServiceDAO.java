package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.Community;
import ru.pocgg.SNSApp.model.CommunityMember;
import ru.pocgg.SNSApp.model.CommunityMemberId;

import java.util.List;

public interface CommunityMemberServiceDAO {
    CommunityMember getMemberById(CommunityMemberId id);
    List<CommunityMember> getMembersByCommunityId(int communityId);
    List<Community> getCommunitiesByMemberId(int memberId);
    List<CommunityMember> getAllMembers();
    void addMember(CommunityMember member);
    void removeMember(CommunityMember member);
    void forceFlush();
}
