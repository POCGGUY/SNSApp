package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.Community;

import java.util.List;

public interface CommunityServiceDAO {
    void addCommunity(Community community);
    void updateCommunity(Community community);
    void deleteCommunity(Community community);
    Community getCommunityById(int id);
    List<Community> getAllCommunities();
    void forceFlush();
    List<Community> searchCommunities(String name);
}
