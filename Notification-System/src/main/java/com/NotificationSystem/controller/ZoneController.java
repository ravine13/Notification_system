package com.NotificationSystem.controller;

import com.NotificationSystem.entities.Zone;
import com.NotificationSystem.service.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/zones")
public class ZoneController {

    @Autowired
    private ZoneService zoneService;

    // Get all zones
    @GetMapping
    public ResponseEntity<List<Zone>> getAllZones() {
        return ResponseEntity.ok(zoneService.getAllZones());
    }

    // Get zone by ID
    @GetMapping("/{id}")
    public ResponseEntity<Zone> getZoneById(@PathVariable Long id) {
        Optional<Zone> zone = zoneService.getZoneById(id);

        return zone.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create zone
    @PostMapping
    public ResponseEntity<Zone> createZone(@RequestBody Zone zone) {
        Zone saved = zoneService.saveZone(zone);
        return ResponseEntity.ok(saved);
    }

    // Update zone
    @PutMapping("/{id}")
    public ResponseEntity<Zone> updateZone(
            @PathVariable Long id,
            @RequestBody Zone updatedZone) {

        Optional<Zone> existing = zoneService.getZoneById(id);

        if (existing.isPresent()) {
            updatedZone.setId(id);
            Zone saved = zoneService.saveZone(updatedZone);
            return ResponseEntity.ok(saved);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    // Delete zone
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {

        if (zoneService.getZoneById(id).isPresent()) {
            zoneService.deleteZone(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

