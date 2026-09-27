package com.NotificationSystem.controller;

import com.NotificationSystem.entities.Collection;
import com.NotificationSystem.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/collections")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @GetMapping
    public ResponseEntity<List<Collection>> getAllCollections() {
        return ResponseEntity.ok(collectionService.getAllCollections());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Collection> getCollectionById(
            @PathVariable Long id) {

        Optional<Collection> collection =
                collectionService.getCollectionById(id);

        return collection.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<Collection>> getCollectionsBySchedule(
            @PathVariable Long scheduleId) {

        return ResponseEntity.ok(
                collectionService.getCollectionsByScheduleId(scheduleId)
        );
    }

    @GetMapping("/resident/{residentId}")
    public ResponseEntity<List<Collection>> getCollectionsByResident(
            @PathVariable Long residentId) {

        return ResponseEntity.ok(
                collectionService.getCollectionsByResidentId(residentId)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Collection>> getCollectionsByStatus(
            @PathVariable Collection.Status status) {

        return ResponseEntity.ok(
                collectionService.getCollectionsByStatus(status)
        );
    }

    @PostMapping
    public ResponseEntity<Collection> createCollection(
            @RequestBody Collection collection) {

        Collection saved =
                collectionService.saveCollection(collection);

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Collection> updateCollection(
            @PathVariable Long id,
            @RequestBody Collection updatedCollection) {

        Optional<Collection> existing =
                collectionService.getCollectionById(id);

        if (existing.isPresent()) {
            updatedCollection.setId(id);
            Collection saved =
                    collectionService.saveCollection(updatedCollection);

            return ResponseEntity.ok(saved);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {

        if (collectionService.getCollectionById(id).isPresent()) {
            collectionService.deleteCollection(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}


