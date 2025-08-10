package ru.pocgg.SNSApp.services.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.services.*;

@Service
@RequiredArgsConstructor
public class PostPermissionService {
    private final PostService postService;
    private final PostCommentService postCommentService;
    private final UserService userService;
    private final FriendshipService friendshipService;
    private final CommunityService communityService;
    private final CommunityMemberService communityMemberService;

    public boolean canCreateComment(int userId, int postId){
        return canViewPost(userId, postId);
    }

    public boolean canViewComment(int userId, int commentId){
        PostComment comment = postCommentService.getCommentById(commentId);
        return canViewPost(userId, comment.getPost().getId());
    }

    public boolean canViewComments(int userId, int postId){
        return canViewPost(userId, postId);
    }

    public boolean canCreateCommunityPost(int authorId, int ownerId) {
        return isUserCommunityModerator(authorId, ownerId) && isCommunityActive(ownerId);
    }

    public boolean canCreateUserPost(int authorId, int ownerId) {
        return isUserActive(ownerId)
                && (authorId == ownerId
                || userService.getUserById(ownerId).getPostsPublic()
                || friendshipService.isFriendshipExist(authorId, ownerId));
    }

    public boolean canModifyPost(int userId, int postId) {
        Post post = postService.getPostById(postId);
        return post.getAuthor().getId() == userId
                && canViewPost(userId, postId);
    }

    public boolean canDeletePost(int userId, int postId) {
        Post post = postService.getPostById(postId);
        if (post.getOwnerCommunity() != null) {
            int communityId = post.getOwnerCommunity().getId();
            return canModifyPost(userId, postId)
                    || isCommunityModerator(userId, communityId)
                    || isSystemModerator(userId);
        } else {
            return canModifyPost(userId, postId)
                    || post.getOwnerUser().getId() == userId
                    || isSystemModerator(userId);
        }
    }

    public boolean canViewPost(int userId, int postId) {
        Post post = postService.getPostById(postId);
        if (post.getDeleted()) return false;

        if (post.getOwnerCommunity() != null) {
            return canViewCommunityPosts(userId, post.getOwnerCommunity().getId());
        } else {
            return canViewUserPosts(userId, post.getOwnerUser().getId());
        }
    }

    public boolean canModifyComment(int userId, int commentId) {
        PostComment comment = postCommentService.getCommentById(commentId);
        return (comment.getAuthor().getId() == userId)
                && canViewPost(userId, comment.getPost().getId()) && !isCommentDeleted(commentId);
    }

    public boolean canDeleteComment(int userId, int commentId) {
        PostComment comment = postCommentService.getCommentById(commentId);
        Post post = postService.getPostById(comment.getPost().getId());

        if (post.getOwnerCommunity() != null) {
            int communityId = post.getOwnerCommunity().getId();
            return canModifyComment(userId, commentId)
                    || isCommunityModerator(userId, communityId)
                    || isSystemModerator(userId);
        } else {
            return canModifyComment(userId, commentId)
                    || post.getOwnerUser().getId() == userId
                    || isSystemModerator(userId);
        }
    }

    public boolean canViewUserPosts(int userId, int targetUserId) {
        User targetUser = userService.getUserById(targetUserId);
        return (userId == targetUserId
                || targetUser.getPostsPublic()
                || friendshipService.isFriendshipExist(userId, targetUserId)
                || isSystemModerator(userId))
                && isUserActive(targetUserId);
    }

    public boolean canViewCommunityPosts(int userId, int communityId) {
        return (!isCommunityPrivate(communityId)
                || communityMemberService.isMemberExist(new CommunityMemberId(communityId, userId))
                || isSystemModerator(userId))
                && isCommunityActive(communityId);
    }

    private Boolean isUserCommunityModerator(int userId, int communityId) {
        if(!communityMemberService.isMemberExist(new CommunityMemberId(communityId, userId))) {
            return false;
        }
        CommunityRole memberRole = communityMemberService.getMemberById(new CommunityMemberId(communityId, userId))
                .getMemberRole();
        return memberRole == CommunityRole.MODERATOR || memberRole == CommunityRole.OWNER;
    }

    private boolean isCommentDeleted(int commentId) {
        return postCommentService.getCommentById(commentId).getDeleted();
    }

    private boolean isCommunityPrivate(int communityId) {
        return communityService.getCommunityById(communityId).getIsPrivate();
    }

    private boolean isCommunityActive(int communityId) {
        Community community = communityService.getCommunityById(communityId);
        return !community.getDeleted() && !community.getBanned();
    }

    private boolean isUserActive(int userId) {
        User user = userService.getUserById(userId);
        return !user.getDeleted() && !user.getBanned();
    }

    private boolean isSystemModerator(int userId) {
        return userService.getUserById(userId).isModerator();
    }

    private boolean isCommunityModerator(int userId, int commId) {
        return communityMemberService
                .getMemberById(new CommunityMemberId(commId, userId))
                .getMemberRole() == CommunityRole.MODERATOR;
    }
}
