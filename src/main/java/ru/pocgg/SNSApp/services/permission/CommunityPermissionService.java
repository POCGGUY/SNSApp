package ru.pocgg.SNSApp.services.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.services.*;

@Service
@RequiredArgsConstructor
public class CommunityPermissionService {
    private final CommunityService communityService;
    private final CommunityMemberService communityMemberService;
    private final CommunityInvitationService communityInvitationService;
    private final UserService userService;

    public boolean canViewCommunity(int userId, int communityId) {
        return isCommunityActive(communityId) &&
                (isCommunityPrivate(communityId)
                || isMember(userId, communityId)
                || isSystemModerator(userId));
    }

    public boolean canDeleteCommunity(int userId, int communityId) {
        return isCommunityActive(communityId) && (isCommunityOwner(userId, communityId) || isSystemModerator(userId));
    }

    public boolean canRemoveMember(int userId, int memberId, int communityId) {
        return canEditCommunity(userId, communityId) && !isMemberOwner(memberId, communityId);
    }

    public boolean canLeaveCommunity(int userId, int communityId) {
        return isMember(userId, communityId) && !isMemberOwner(userId, communityId);
    }

    public boolean canChangeRole(int userId, int communityId) {
        return isCommunityOwner(userId, communityId);
    }

    public boolean canViewMembers(int userId, int communityId) {
        return canViewCommunity(userId, communityId);
    }

    public boolean canBecomeMember(int userId, int communityId) {
        return isCommunityActive(communityId) && !isCommunityPrivate(communityId) && !isMember(userId, communityId);
    }

    public boolean canInviteToCommunity(int senderId, int receiverId, int communityId) {
        return isUserActive(receiverId) &&
                isCommunityActive(communityId) &&
                !isInvitationExist(receiverId, communityId) &&
                !isCommunityMember(receiverId, communityId) &&
                (!isCommunityPrivate(communityId) || isCommunityModerator(senderId, communityId));
    }

    public boolean canEditCommunity(int userId, int communityId) {
        return isCommunityModerator(userId, communityId);
    }

    public boolean canViewInvitations(int userId, int communityId) {
        return isCommunityModerator(userId, communityId) || isSystemModerator(userId);
    }

    public boolean canDeleteInvitation(int userId, int senderId, int receiverId, int communityId) {
        CommunityInvitationId id = CommunityInvitationId.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .communityId(communityId)
                .build();
        CommunityInvitation inv = communityInvitationService.getInvitationById(id);
        return inv.getSender().getId() == userId
                || isCommunityModerator(userId, id.getCommunityId());
    }

    private boolean isCommunityOwner(int userId, int communityId) {
        Community community = communityService.getCommunityById(communityId);
        return community.getOwner().getId() == userId;
    }

    private boolean isCommunityActive(int communityId) {
        Community community = communityService.getCommunityById(communityId);
        return !community.getDeleted() || !community.getBanned();
    }

    private boolean isUserActive(int userId) {
        User user = userService.getUserById(userId);
        return !user.getDeleted() && !user.getBanned();
    }

    private boolean isMember(int userId, int communityId) {
        return communityMemberService.isMemberExist(
                new CommunityMemberId(communityId, userId));
    }

    private boolean isCommunityModerator(int userId, int communityId) {
        if (!isMember(userId, communityId)) return false;
        return communityMemberService.getMemberById(
                        new CommunityMemberId(communityId, userId))
                .getMemberRole() == CommunityRole.MODERATOR;
    }

    private boolean isSystemModerator(int userId) {
        return userService.getUserById(userId).isModerator();
    }

    private boolean isCommunityMember(int userId, int communityId) {
        return communityMemberService.isMemberExist(CommunityMemberId.builder()
                .memberId(userId)
                .communityId(communityId)
                .build());
    }

    private boolean isCommunityPrivate(int communityId) {
        return communityService.getCommunityById(communityId).getIsPrivate();
    }

    private boolean isInvitationExist(int receiverId, int communityId) {
        return communityInvitationService
                .isInvitationExist(CommunityInvitationId.builder()
                        .receiverId(receiverId)
                        .communityId(communityId)
                        .build());
    }

    private boolean isMemberOwner(int userId, int communityId) {
        CommunityMemberId id = CommunityMemberId.builder()
                .memberId(userId)
                .communityId(communityId)
                .build();
        CommunityMember member = communityMemberService.getMemberById(id);
        return member.getMemberRole() == CommunityRole.OWNER;
    }
}
