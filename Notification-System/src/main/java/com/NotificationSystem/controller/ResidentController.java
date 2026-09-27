package com.NotificationSystem.controller;

import com.NotificationSystem.entities.Resident;
import com.NotificationSystem.service.ResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/residents")
public class ResidentController {

    @Autowired
    private ResidentService residentService;

    @GetMapping
    public ResponseEntity<List<Resident>> getAllResidents() {
        return ResponseEntity.ok(residentService.getAllResidents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resident> getResidentById(@PathVariable Long id) {
        Optional<Resident> resident = residentService.getResidentById(id);

        return resident.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<Resident>> getResidentsByZone(
            @PathVariable Long zoneId) {

        return ResponseEntity.ok(
                residentService.getResidentsByZoneId(zoneId)
        );
    }

    @PostMapping
    public ResponseEntity<Resident> createResident(
            @RequestBody Resident resident) {

        Resident saved = residentService.saveResident(resident);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resident> updateResident(
            @PathVariable Long id,
            @RequestBody Resident updatedResident) {

        Optional<Resident> existing =
                residentService.getResidentById(id);

        if (existing.isPresent()) {
            updatedResident.setId(id);
            Resident saved = residentService.saveResident(updatedResident);
            return ResponseEntity.ok(saved);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResident(@PathVariable Long id) {

        if (residentService.getResidentById(id).isPresent()) {
            residentService.deleteResidentById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

