package ru.pocgg.SNSApp.services;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import ru.pocgg.SNSApp.DTO.mappers.update.UpdateCommunityMapper;
import ru.pocgg.SNSApp.model.Community;
import ru.pocgg.SNSApp.DTO.create.CreateCommunityDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateCommunityDTO;
import ru.pocgg.SNSApp.events.events.CommunityBecamePublicEvent;
import ru.pocgg.SNSApp.events.events.CommunityCreatedEvent;
import ru.pocgg.SNSApp.events.events.CommunityDeactivatedEvent;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.CommunityMemberServiceDAO;
import ru.pocgg.SNSApp.services.DAO.interfaces.CommunityServiceDAO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityService extends TemplateService {

    private final CommunityServiceDAO communityServiceDAO;
    private final UserService userService;
    private final CommunityMemberServiceDAO communityMemberServiceDAO;
    private final UpdateCommunityMapper updateCommunityMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.events.exchange}")
    private String exchangeName;

    public Community createCommunity(int ownerId, CreateCommunityDTO dto) {
        Community community = Community.builder()
                .owner(userService.getUserById(ownerId))
                .communityName(dto.getCommunityName())
                .creationDate(Instant.now())
                .description(dto.getDescription())
                .isPrivate(dto.getIsPrivate())
                .deleted(false)
                .banned(false)
                .build();
        communityServiceDAO.addCommunity(community);
        communityServiceDAO.forceFlush();
        CommunityCreatedEvent event = CommunityCreatedEvent.builder()
                .communityId(community.getId())
                .ownerId(ownerId)
                .build();
        rabbitTemplate.convertAndSend(exchangeName, "community.created", event);
        logCommunityCreated(community);
        return community;
    }

    public void updateCommunity(int communityId, UpdateCommunityDTO dto) {
        Community community = getCommunityById(communityId);
        updateCommunityMapper.updateFromDTO(dto, community);
        logger.info("updated community with id: {}", communityId);
    }

    @Transactional(readOnly = true)
    public List<Community> getAllCommunities() {
        return communityServiceDAO.getAllCommunities();
    }

    @Transactional(readOnly = true)
    public List<Community> getCommunitiesByMemberId(int memberId) {
        return communityMemberServiceDAO.getCommunitiesByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public Community getCommunityById(int communityId) {
        return getCommunityByIdOrThrowException(communityId);
    }

    @Transactional(readOnly = true)
    public List<Community> searchCommunities(String communityName) {
        return communityServiceDAO.searchCommunities(communityName);
    }

    public void setIsDeleted(int communityId, boolean value) {
        Community community = getCommunityByIdOrThrowException(communityId);
        if (community.getDeleted().equals(value)) {
            logger.info("community with id: {} has been already set deleted to: {}", communityId, value);
        } else {
            community.setDeleted(value);
            if (value) {
                CommunityDeactivatedEvent event = CommunityDeactivatedEvent.builder()
                        .communityId(community.getId())
                        .build();
                rabbitTemplate.convertAndSend(exchangeName, "community.deactivated", event);
            }
            logger.info("community with id: {} now deleted", communityId);
        }
    }

    public void setIsBanned(int communityId, boolean value) {
        Community community = getCommunityByIdOrThrowException(communityId);
        if (community.getBanned().equals(value)) {
            logger.info("community with id: {} has been already set banned to: {}", communityId, value);
        } else {
            community.setBanned(value);
            if (value) {
                CommunityDeactivatedEvent event = CommunityDeactivatedEvent.builder()
                        .communityId(community.getId())
                        .build();
                rabbitTemplate.convertAndSend(exchangeName, "community.deactivated", event);
            }
            logger.info("community with id: {} now is set banned to: {}", communityId, value);
        }
    }

    private Community getCommunityByIdOrThrowException(int communityId) {
        Community community = communityServiceDAO.getCommunityById(communityId);
        if (community != null) {
            return community;
        } else {
            throw new EntityNotFoundException("community with id: " + communityId + " not found");
        }
    }

    private void logCommunityCreated(Community community) {
        logger.info("community with id: {} has been created by user with id: {}",
                community.getId(), community.getOwner().getId());
    }
}
