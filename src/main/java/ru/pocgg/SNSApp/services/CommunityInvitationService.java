package ru.pocgg.SNSApp.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import ru.pocgg.SNSApp.model.CommunityInvitation;
import ru.pocgg.SNSApp.model.CommunityInvitationId;
import ru.pocgg.SNSApp.DTO.create.CreateCommunityInvitationDTO;
import ru.pocgg.SNSApp.events.events.CommunityInvitationAcceptedEvent;
import ru.pocgg.SNSApp.events.events.CommunityInvitationDeclinedEvent;
import ru.pocgg.SNSApp.events.events.CommunityInvitationCreatedEvent;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.CommunityInvitationServiceDAO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityInvitationService extends TemplateService {
    private final CommunityInvitationServiceDAO communityInvitationServiceDAO;
    private final UserService userService;
    private final CommunityService communityService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.events.exchange}")
    private String exchangeName;

    public CommunityInvitation createInvitation(int senderId, int receiverId, int communityId,
                                                Instant creationDate, CreateCommunityInvitationDTO dto) {
        CommunityInvitation invitation = CommunityInvitation.builder()
                .sender(userService.getUserById(senderId))
                .receiver(userService.getUserById(receiverId))
                .community(communityService.getCommunityById(communityId))
                .creationDate(creationDate)
                .description(dto.getDescription()).build();
        communityInvitationServiceDAO.addInvitation(invitation);
        CommunityInvitationCreatedEvent event = CommunityInvitationCreatedEvent.builder()
                .id(invitation.getId())
                .build();
        rabbitTemplate.convertAndSend(exchangeName, "community.invitation.created", event);
        logger.info("created community invitation from user with id: " +
                        "{} to user with id: {} in community with id: {}",
                senderId, receiverId, communityId);
        return invitation;
    }

    public boolean isInvitationExist(CommunityInvitationId invitationId) {
        return communityInvitationServiceDAO.getInvitationById(invitationId) != null;
    }

    public List<CommunityInvitation> getInvitationsBySenderId(int senderId) {
        return communityInvitationServiceDAO.getInvitationsBySenderId(senderId);
    }

    public List<CommunityInvitation> getInvitationsByReceiverId(int receiverId) {
        return communityInvitationServiceDAO.getInvitationsByReceiverId(receiverId);
    }

    public List<CommunityInvitation> getInvitationsByCommunityId(int communityId) {
        return communityInvitationServiceDAO.getInvitationsByCommunityId(communityId);
    }

    public CommunityInvitation getInvitationById(CommunityInvitationId id) {
        return getInvitationByIdOrThrowException(id);
    }

    public boolean isUserAlreadyInvited(int receiverId, int communityId) {
        List<CommunityInvitation> list =
                communityInvitationServiceDAO.getInvitationsByReceiverAndCommunityId(receiverId, communityId);
        return list != null && !list.isEmpty();
    }

    public void acceptInvitation(CommunityInvitationId id) {
        CommunityInvitation invitation = getInvitationByIdOrThrowException(id);
        CommunityInvitationAcceptedEvent event = CommunityInvitationAcceptedEvent.builder()
                .id(id)
                .build();
        rabbitTemplate.convertAndSend(exchangeName, "community.invitation.accepted", event);
        communityInvitationServiceDAO.removeInvitation(invitation);
        logger.info("User with id: {} has accepted invitation to community with id: {}",
                id.getReceiverId(), id.getCommunityId());
    }

    public void declineInvitation(CommunityInvitationId id) {
        CommunityInvitation invitation = getInvitationByIdOrThrowException(id);
        CommunityInvitationDeclinedEvent event = CommunityInvitationDeclinedEvent.builder()
                .id(id)
                .build();
        rabbitTemplate.convertAndSend(exchangeName, "community.invitation.declined", event);
        communityInvitationServiceDAO.removeInvitation(invitation);
        logger.info("User with id: {} has declined invitation to community with id: {}",
                id.getReceiverId(), id.getCommunityId());
    }

    public void removeInvitation(CommunityInvitationId id) {
        CommunityInvitation invitation = getInvitationByIdOrThrowException(id);
        communityInvitationServiceDAO.removeInvitation(invitation);
        logger.info("removed community invitation with id: {}", id);
    }

    public void removeByCommunityId(int communityId) {
        communityInvitationServiceDAO.removeByCommunityId(communityId);
        logger.info("all invitations in community with id: {} has been removed", communityId);
    }

    public void removeByReceiverId(int receiverId) {
        communityInvitationServiceDAO.removeByReceiverId(receiverId);
        logger.info("all community invitations to receiver with id: {} has been removed", receiverId);
    }

    public void removeBySenderId(int senderId) {
        communityInvitationServiceDAO.removeBySenderId(senderId);
        logger.info("all community invitations by sender with id: {} has been removed", senderId);
    }

    private CommunityInvitation getInvitationByIdOrThrowException(CommunityInvitationId id) {
        CommunityInvitation invitation = communityInvitationServiceDAO.getInvitationById(id);
        if (invitation == null) {
            throw new EntityNotFoundException("invitation to community with id: " + id.getCommunityId()
                    + " by a sender with id: " + id.getSenderId() + " to receiver with id: " + id.getReceiverId() +
                    " not found");
        }
        return invitation;
    }

}
