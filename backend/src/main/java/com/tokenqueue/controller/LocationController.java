package com.tokenqueue.controller;

import com.tokenqueue.dto.TokenDTO.*;
import com.tokenqueue.model.Location;
import com.tokenqueue.model.ServiceEntity;
import com.tokenqueue.repository.LocationRepository;
import com.tokenqueue.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        List<LocationDTO> locations = locationRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocation(@PathVariable Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        return ResponseEntity.ok(mapToDTO(location));
    }

    @GetMapping("/{id}/services")
    public ResponseEntity<List<ServiceDTO>> getServices(@PathVariable Long id) {
        List<ServiceDTO> services = serviceRepository.findByLocationIdAndActiveTrue(id).stream()
                .map(this::mapServiceToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(services);
    }

    private LocationDTO mapToDTO(Location location) {
        List<ServiceDTO> services = serviceRepository.findByLocationIdAndActiveTrue(location.getId())
                .stream()
                .map(this::mapServiceToDTO)
                .collect(Collectors.toList());

        return LocationDTO.builder()
                .id(location.getId())
                .name(location.getName())
                .address(location.getAddress())
                .city(location.getCity())
                .type(location.getType())
                .description(location.getDescription())
                .imageUrl(location.getImageUrl())
                .active(location.isActive())
                .services(services)
                .build();
    }

    private ServiceDTO mapServiceToDTO(ServiceEntity service) {
        return ServiceDTO.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .avgServiceTime(service.getAvgServiceTime())
                .active(service.isActive())
                .build();
    }
}
