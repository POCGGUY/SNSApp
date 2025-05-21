package ru.pocgg.SNSApp.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ru.pocgg.SNSApp.model.CommunityMember;
import ru.pocgg.SNSApp.model.CommunityMemberId;
import ru.pocgg.SNSApp.model.CommunityRole;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.CommunityMemberServiceDAO;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityMemberService extends TemplateService{
    private final CommunityMemberServiceDAO communityMemberServiceDAO;
    private final CommunityService communityService;
    private final UserService userService;

    public CommunityMember createMember(int communityId, int memberId, Instant entryDate) {
        CommunityMember member = CommunityMember.builder()
                .community(communityService.getCommunityById(communityId))
                .member(userService.getUserById(memberId))
                .memberRole(CommunityRole.MEMBER)
                .entryDate(entryDate).build();
        communityMemberServiceDAO.addMember(member);
        logger.info("added user with id: {} to community with id: {}", memberId, communityId);
        return member;
    }

    public CommunityMember createMember(int communityId, int memberId, Instant entryDate, CommunityRole communityRole) {
        CommunityMember member = new CommunityMember(
                communityService.getCommunityById(communityId),
                userService.getUserById(memberId),
                entryDate,
                communityRole
        );
        communityMemberServiceDAO.addMember(member);
        logger.info("added user with id: {} to community with id: {} as owner", memberId, communityId);
        return member;
    }

    public List<CommunityMember> getMembersByCommunityId(int communityId) {
        return communityMemberServiceDAO.getMembersByCommunityId(communityId);
    }

    public void setMemberRole(CommunityMemberId memberId, CommunityRole role) {
        CommunityMember member = getMemberByIdOrThrowException(memberId);
        member.setMemberRole(role);
        logger.info("user with id: " + memberId.getMemberId() + " in community with id: " +
                memberId.getCommunityId() + "  now have community role set to : " + role.toString());
    }

    public List<CommunityMember> getAllMembers() {
        return communityMemberServiceDAO.getAllMembers();
    }

    public CommunityMember getMemberById(CommunityMemberId id) {
        return getMemberByIdOrThrowException(id);
    }

    public void removeMember(CommunityMemberId id) {
        CommunityMember member = getMemberByIdOrThrowException(id);
        communityMemberServiceDAO.removeMember(member);
        logger.info("removed member with id: {} from community with id: {}", id.getMemberId(), id.getCommunityId());
    }

    public Boolean isMemberExist(CommunityMemberId id) {
        return communityMemberServiceDAO.getMemberById(id) != null;
    }

    private CommunityMember getMemberByIdOrThrowException(CommunityMemberId id) {
        CommunityMember member = communityMemberServiceDAO.getMemberById(id);
        if (member == null) {
            throw new EntityNotFoundException("member with id: " + id.getMemberId() +  " in community with id:"
                    + id.getCommunityId()+ " not found");
        }
        return member;
    }
}
