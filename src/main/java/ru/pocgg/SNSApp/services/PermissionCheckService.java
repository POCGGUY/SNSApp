package ru.pocgg.SNSApp.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pocgg.SNSApp.model.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionCheckService {
    private final ChatMemberService chatMemberService;
    private final CommunityMemberService communityMemberService;
    private final ChatMessageService chatMessageService;
    private final CommunityInvitationService communityInvitationService;
    private final UserService userService;
    private final ChatService chatService;
    private final CommunityService communityService;
    private final FriendshipService friendshipService;
    private final PrivateMessageService privateMessageService;
    private final PostService postService;
    private final PostCommentService postCommentService;


    public boolean canUserViewInvitationsInChat(int userId, int chatId){
        return isUserSystemModerator(userId) || (isUserChatOwner(userId, chatId) && isChatMemberExist(userId, chatId));
    }

    public boolean canEditChat(int userId, int chatId){
        return isUserChatOwnerOrSystemModerator(userId, chatId) && isChatActive(chatId);
    }

    public boolean canViewChat(int userId, Chat chat){
        return (!chat.isPrivate() || isUserChatMember(userId, chat.getId())) && isChatActive(chat.getId());
    }

    public boolean canViewInvitationsInCommunity(int userId, int communityId){
        return isUserCommunityModerator(userId, communityId) || isUserSystemModerator(userId);
    }

    public boolean canUserCreateMessageInChat(int userId, int chatId){
        return isUserChatMember(userId, chatId) && isChatActive(chatId);
    }

    public boolean canSendFriendRequest(int userId){
        return isUserActive(userId);
    }

    public boolean isUserChatMember(int userId, int chatId) {
        return chatMemberService.isChatMemberExist(new ChatMemberId(chatId, userId));
    }

    public boolean canUserViewMessagesInChat(int userId, int chatId) {
        return isUserChatMember(userId, chatId) && isChatActive(chatId);
    }

    public boolean canUserModifyChatMessage(int userId, int messageId) {
        ChatMessage message = chatMessageService.getChatMessageById(messageId);
        int senderId = message.getSender().getId();
        return isChatActive(message.getChat().getId())
                && isUserChatMember(userId, message.getChat().getId()) && senderId == userId;
    }

    public boolean canUserCreateUserPost(int authorId, int ownerId) {
        return (authorId == ownerId
                || userService.getUserById(ownerId).getPostsPublic()
                || friendshipService.isFriendshipExist(authorId, ownerId)) && isUserActive(ownerId);
    }

    public boolean canUserDeletePost(int userId, int postId) {
        Post post = postService.getPostById(postId);
        if(post.getOwnerCommunity() != null){
            return (canUserModifyPost(userId, postId)
                    || isUserSystemModerator(userId)
                    || isUserCommunityModerator(userId, post.getOwnerCommunity().getId()));
        } else {
            return (canUserModifyPost(userId, postId) || isUserSystemModerator(userId)
                    || userId == post.getOwnerUser().getId());
        }
    }

    public boolean canUserDeletePostComment(int userId, int commentId) {
        PostComment comment = postCommentService.getCommentById(commentId);
        Post post = postService.getPostById(comment.getPost().getId());
        if(post.getOwnerCommunity() != null){
            return (canUserModifyPostComment(userId, commentId))
                    || isUserSystemModerator(userId)
                    || isUserCommunityModerator(userId, post.getOwnerCommunity().getId());
        } else {
            return canUserModifyPostComment(userId, commentId)
                    || isUserPostOwner(userId, post)
                    || isUserSystemModerator(userId);
        }
    }

    public boolean isUserPostOwner(int userId, Post post) {
        return userId == post.getOwnerUser().getId();
    }

    public boolean canUserViewPost(int userId, int postId) {
        Post post = postService.getPostById(postId);
        if(post.getDeleted()){
            return false;
        }
        if(post.getOwnerCommunity() != null){
            return (canViewPostsAtCommunity(userId, post.getOwnerCommunity().getId()));
        } else {
            return (canViewPostsAtUser(userId, post.getOwnerUser().getId()));
        }
    }

    public boolean canUserModifyPostComment(int userId, int commentId) {
        PostComment postComment = postCommentService.getCommentById(commentId);
        return userId == postComment.getAuthor().getId() && canUserViewPost(userId, postComment.getPost().getId());
    }

    public boolean canViewPostsAtUser(int userId, int targetUserId) {
        User target = userService.getUserById(targetUserId);
        return (userId == targetUserId || target.getPostsPublic() || friendshipService.isFriendshipExist(userId, targetUserId)
                || isUserSystemModerator(userId)) && (isUserActive(targetUserId)|| isUserSystemModerator(userId));
    }

    public boolean canViewUserProfile(int userId, int targetUserId) {
        return isUserActive(targetUserId) || isUserSystemModerator(userId);
    }

    public boolean canViewPostsAtCommunity(int userId, int communityId) {
        return (!isCommunityPrivate(communityId) || isUserCommunityMember(userId, communityId)
                || isUserSystemModerator(userId)) && isCommunityActive(communityId);
    }

    public boolean canUserCreateCommunityPost(int authorId, int ownerId) {
        return isUserCommunityModerator(authorId, ownerId) && isCommunityActive(ownerId);
    }

    public boolean canUserModifyPost(int userId, int postId) {
        Post post = postService.getPostById(postId);
        return userId == post.getAuthor().getId() && canUserViewPost(userId, postId);
    }

    public boolean canUserModifyPrivateMessage(int userId, int messageId) {
        PrivateMessage message = privateMessageService.getById(messageId);
        int senderId = message.getSender().getId();
        return userId == senderId;
    }

    public boolean canUserDeleteCommunityInvitation(int userId, CommunityInvitationId id) {
        CommunityInvitation communityInvitation = communityInvitationService.getInvitationById(id);
        int senderId = communityInvitation.getSender().getId();
        return userId == senderId || isUserCommunityModerator(userId, communityInvitation.getCommunity().getId());
    }

    public boolean canUserReadPrivateMessage(int userId, int messageId) {
        PrivateMessage message = privateMessageService.getById(messageId);
        int senderId = message.getSender().getId();
        int receiverId = message.getReceiver().getId();
        return userId == receiverId || userId == senderId;
    }

    public boolean canUserDeletePrivateMessage(int userId, int messageId) {
        return canUserModifyPrivateMessage(userId, messageId) || isUserSystemModerator(userId);
    }

    public Boolean canUserViewCommunity(int userId, int communityId) {
        return !isCommunityPrivate(communityId)
                || isUserCommunityMember(userId, communityId)
                || isUserSystemModerator(userId);
    }

    public Boolean canUserEditCommunity(int userId, int communityId) {
        return isUserCommunityMember(userId, communityId) && isUserCommunityModerator(userId, communityId);
    }

    public Boolean isUserCommunityMember(int userId, int communityId) {
        return communityMemberService.isMemberExist(new CommunityMemberId(communityId, userId));
    }

    public Boolean isUserCommunityModerator(int userId, int communityId) {
        if(!communityMemberService.isMemberExist(new CommunityMemberId(communityId, userId))) {
            return false;
        }
        CommunityRole memberRole = communityMemberService.getMemberById(new CommunityMemberId(communityId, userId))
                .getMemberRole();
        return memberRole == CommunityRole.MODERATOR || memberRole == CommunityRole.OWNER;
    }

    public Boolean isUserCommunityOwner(int userId, int communityId) {
        Community community = communityService.getCommunityById(communityId);
        return isUserCommunityMember(userId, communityId)
                && (communityMemberService.getMemberById(new CommunityMemberId(communityId, userId))
                .getMemberRole() == CommunityRole.OWNER || userId == community.getOwner().getId());
    }

    public boolean canUserDeleteChatMessage(int userId, int messageId) {
        ChatMessage message = chatMessageService.getChatMessageById(messageId);
        return canUserModifyChatMessage(userId, messageId) ||
                (isUserChatOwnerOrSystemModerator(userId, message.getChat().getId())
                        && isChatActive(message.getChat().getId()));
    }

    public boolean isUserSystemModerator(int userId){
        return userService.getUserById(userId).isModerator();
    }

    public boolean isUserChatOwnerOrSystemModerator(int userId, int chatId) {
        return isUserChatOwner(userId, chatId) || isUserSystemModerator(userId);
    }

    public boolean isUserChatOwner(int userId, int chatId) {
        return chatService.getChatById(chatId).getOwner().getId() == userId;
    }

    public boolean isChatMemberExist(int userId, int chatId) {
        return chatMemberService.isChatMemberExist(new ChatMemberId(chatId, userId));
    }

    public boolean canSendMessageToThisUser(int senderId, int receiverId) {
        User receiver = userService.getUserById(receiverId);
        if (receiver.getAcceptingPrivateMsgs() && isUserActive(receiver)) {
            return true;
        } else {
            return friendshipService.isFriendshipExist(senderId, receiverId) && isUserActive(receiver);
        }
    }

    public boolean isUserActive(User user) {
        return !user.getDeleted() && !user.getBanned();
    }

    public boolean isUserActive(int userId) {
        User user = userService.getUserById(userId);
        return !user.getDeleted() && !user.getBanned();
    }

    public boolean isCommunityActive(int communityId) {
        Community community = communityService.getCommunityById(communityId);
        return !community.getDeleted() && !community.getBanned();
    }

    public boolean isChatActive(int chatId) {
        Chat chat = chatService.getChatById(chatId);
        return !chat.isDeleted();
    }

    private Boolean isCommunityPrivate(int communityId) {
        return communityService.getCommunityById(communityId).getIsPrivate();
    }
}
