package com.NotificationSystem.repositories;



import com.NotificationSystem.entities.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    Optional<Collection> getCollectionById(Long id);
    List<Collection> findByScheduleId(Long scheduleId);

    List<Collection> findByResidentId(Long residentId);

    List<Collection> findByStatus(Collection.Status status);

    List<Collection> findByScheduleIdAndResidentId(
            Long scheduleId,
            Long residentId
    );

}

