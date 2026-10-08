package com.NotificationSystem.controller;

import com.NotificationSystem.entities.Truck;
import com.NotificationSystem.repositories.TruckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trucks")
public class TruckController {

    @Autowired
    private TruckRepository truckRepository;

    @GetMapping
    public List<Truck> getAll() {
        return truckRepository.findAll();
    }

    @PostMapping
    public Truck create(@RequestBody Truck truck) {
        truck.setId(null);
        return truckRepository.save(truck);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Truck> update(@PathVariable Long id, @RequestBody Truck in) {
        return truckRepository.findById(id).map(t -> {
            t.setPlateNumber(in.getPlateNumber());
            t.setModel(in.getModel());
            t.setCapacityTons(in.getCapacityTons());
            t.setDriverName(in.getDriverName());
            if (in.getStatus() != null) t.setStatus(in.getStatus());
            return ResponseEntity.ok(truckRepository.save(t));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!truckRepository.existsById(id)) return ResponseEntity.notFound().build();
        truckRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}