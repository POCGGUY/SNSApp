package ru.pocgg.SNSApp.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final CommunityMemberServiceDAO communityMemberServiceDAO;

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
        eventPublisher.publishEvent(CommunityCreatedEvent.builder()
                .communityId(community.getId())
                .ownerId(ownerId)
                .build());
        logCommunityCreated(community);
        return community;
    }

    public void updateCommunity(int communityId, UpdateCommunityDTO dto) {
        Community community = getCommunityById(communityId);
        updateCommunityName(community, dto.getCommunityName());
        updateDescription(community, dto.getDescription());
        updateIsPrivate(community, dto.getIsPrivate());
        logger.info("updated community with id: {}", communityId);
    }

    public List<Community> getAllCommunities() {
        return communityServiceDAO.getAllCommunities();
    }

    public List<Community> getCommunitiesByMemberId(int memberId) {
        return communityMemberServiceDAO.getCommunitiesByMemberId(memberId);
    }

    public Community getCommunityById(int communityId) {
        return getCommunityByIdOrThrowException(communityId);
    }

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
                eventPublisher.publishEvent(new CommunityDeactivatedEvent(communityId));
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
                eventPublisher.publishEvent(new CommunityDeactivatedEvent(communityId));
            }
            logger.info("community with id: {} now is set banned to: {}", communityId, value);
        }
    }

    private void updateCommunityName(Community community, String name) {
        if (name != null) {
            community.setCommunityName(name);
            logger.info("community with id: {} has updated name", community.getId());
        }
    }

    private void updateDescription(Community community, String description) {
        if (description != null) {
            community.setDescription(description);
            logger.info("community with id: {} has updated description", community.getId());
        }
    }

    private void updateIsPrivate(Community community, Boolean isPrivate) {
        if (isPrivate != null) {
            community.setIsPrivate(isPrivate);
            if (!isPrivate) {
                eventPublisher.publishEvent(CommunityBecamePublicEvent.builder()
                        .communityId(community.getId())
                        .build());
            }
            logger.info("community with id: {} has updated private property, now its: {} ",
                    community.getId(), isPrivate);
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
