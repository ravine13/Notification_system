package com.NotificationSystem.service;



import com.NotificationSystem.entities.Resident;
import com.NotificationSystem.repositories.ResidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ResidentService {

    @Autowired
    private ResidentRepository residentRepository;

    // Get all residents
    public List<Resident> getAllResidents() {
        return residentRepository.findAll();
    }

    // Get resident by ID
    public Optional<Resident> getResidentById(Long id) {
        return residentRepository.findById(id);
    }

    // Create or update resident
    public Resident saveResident(Resident resident) {
        LocalDateTime now = LocalDateTime.now();
        if(resident.getId() == null) {
            resident.setCreatedAt(now);
        }
        resident.setUpdatedAt(now);
        return residentRepository.save(resident);
    }

    // Delete resident by ID
    public void deleteResidentById(Long id) {
        residentRepository.deleteById(id);
    }

//    }
    public List<Resident> updateResidents(List<Resident> residents) {
        return residentRepository.saveAll(residents);
    }
    public List<Resident>getResidentsByZoneId(Long zoneId) {
        return residentRepository.findByZone_Id(zoneId);
    }

}

