package com.NotificationSystem.service;

import com.NotificationSystem.entities.Zone;
import com.NotificationSystem.repositories.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ZoneService {

    @Autowired
    private ZoneRepository zoneRepository;

    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }
    public Optional<Zone> getZoneById(Long id) {
        return zoneRepository.findById(id);
    }
    public Zone saveZone(Zone zone) {
        LocalDateTime now = LocalDateTime.now();
        if(zone.getId() == null) {
            zone.setCreatedAt(now);
        }
        zone.setUpdatedAt(now);
        return zoneRepository.save(zone);
    }
    public void deleteZone(Long id) {
        zoneRepository.deleteById(id);
    }
}

