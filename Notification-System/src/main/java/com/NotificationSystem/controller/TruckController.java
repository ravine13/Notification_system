package com.NotificationSystem.controller;

import com.NotificationSystem.dto.TruckRequest;
import com.NotificationSystem.entities.Truck;
import com.NotificationSystem.repositories.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/trucks")
@RequiredArgsConstructor
public class TruckController {

    private final TruckRepository truckRepository;

    @GetMapping
    public List<Truck> getAll() {
        return truckRepository.findAll(Sort.by("plateNumber"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Truck> getOne(@PathVariable Long id) {
        return truckRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TruckRequest in) {
        String problem = validate(in);
        if (problem != null) return error(HttpStatus.BAD_REQUEST, problem);

        String plate = normalizePlate(in.plateNumber());
        if (truckRepository.existsByPlateNumberIgnoreCase(plate)) {
            return error(HttpStatus.CONFLICT, "A truck with plate " + plate + " already exists.");
        }

        Truck truck = new Truck();
        apply(truck, in, plate);
        return save(truck, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TruckRequest in) {
        Optional<Truck> found = truckRepository.findById(id);
        if (found.isEmpty()) return ResponseEntity.notFound().build();

        String problem = validate(in);
        if (problem != null) return error(HttpStatus.BAD_REQUEST, problem);

        String plate = normalizePlate(in.plateNumber());
        if (truckRepository.existsByPlateNumberIgnoreCaseAndIdNot(plate, id)) {
            return error(HttpStatus.CONFLICT, "Another truck already uses plate " + plate + ".");
        }

        Truck truck = found.get();
        apply(truck, in, plate);
        return save(truck, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!truckRepository.existsById(id)) return ResponseEntity.notFound().build();


        long used = truckRepository.countSchedulesUsingTruck(id);
        if (used > 0) {
            return error(HttpStatus.CONFLICT,
                    "This truck is used by " + used + (used == 1 ? " schedule" : " schedules")
                            + ", so it can't be deleted. Set it to Maintenance instead, "
                            + "or move those schedules to another truck first.");
        }

        try {
            truckRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            return error(HttpStatus.CONFLICT, "This truck is still referenced by other records.");
        }
        return ResponseEntity.noContent().build();
    }



    private String validate(TruckRequest in) {
        if (in == null) return "Request body is required.";
        if (in.plateNumber() == null || in.plateNumber().isBlank()) return "Plate number is required.";
        if (normalizePlate(in.plateNumber()).length() > 20) return "Plate number can be at most 20 characters.";
        if (in.capacityTons() != null && in.capacityTons().signum() < 0) return "Capacity cannot be negative.";
        return null;
    }


    private String normalizePlate(String raw) {
        return raw.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private void apply(Truck truck, TruckRequest in, String plate) {
        truck.setPlateNumber(plate);
        truck.setModel(blankToNull(in.model()));
        truck.setCapacityTons(in.capacityTons());
        truck.setDriverName(blankToNull(in.driverName()));
        if (in.status() != null) truck.setStatus(in.status());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }


    private ResponseEntity<?> save(Truck truck, HttpStatus okStatus) {
        try {
            return ResponseEntity.status(okStatus).body(truckRepository.save(truck));
        } catch (DataIntegrityViolationException e) {
            return error(HttpStatus.CONFLICT, "A truck with this plate number already exists.");
        }
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}