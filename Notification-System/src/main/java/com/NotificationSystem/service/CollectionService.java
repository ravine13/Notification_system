package com.NotificationSystem.service;


import com.NotificationSystem.entities.Collection;
import com.NotificationSystem.repositories.CollectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CollectionService {

    @Autowired
    private CollectionRepository collectionRepository;

    // Get all collections
    public List<Collection> getAllCollections() {
        return collectionRepository.findAll();
    }

    // Get collection by ID
    public Optional<Collection> getCollectionById(Long id) {
        return collectionRepository.findById(id);
    }

    // Create or update collection
    public Collection saveCollection(Collection collection) {
        return collectionRepository.save(collection);
    }

    // Delete collection by ID
    public void deleteCollection(Long id) {
        collectionRepository.deleteById(id);
    }

    // Get collections by status
    public List<Collection> getCollectionsByStatus(Collection.Status status) {
        return collectionRepository.findByStatus(status);
    }

    public List<Collection> getCollectionsByResidentId(Long residentId) {
        return collectionRepository.findByResidentId(residentId);
    }
    public List<Collection> getCollectionsByScheduleId(Long scheduleId) {
        return collectionRepository.findByScheduleId(scheduleId);
    }

}
