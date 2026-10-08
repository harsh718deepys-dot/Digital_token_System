package com.tokenqueue.controller;

import com.tokenqueue.dto.TokenDTO.*;
import com.tokenqueue.model.Counter;
import com.tokenqueue.model.Location;
import com.tokenqueue.repository.CounterRepository;
import com.tokenqueue.repository.LocationRepository;
import com.tokenqueue.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private LocationRepository locationRepository;

    // ===== Dashboard =====

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(tokenService.getDashboard());
    }

    // ===== Token Management =====

    @GetMapping("/tokens")
    public ResponseEntity<List<TokenResponse>> getAllTokens() {
        return ResponseEntity.ok(tokenService.getAllTokens());
    }

    @PostMapping("/queue/call-next")
    public ResponseEntity<?> callNextToken(@RequestBody Map<String, Long> request) {
        try {
            Long serviceId = request.get("serviceId");
            Long counterId = request.get("counterId");
            CallNextResponse response = tokenService.callNextToken(serviceId, counterId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/tokens/{id}/complete")
    public ResponseEntity<?> completeToken(@PathVariable Long id) {
        try {
            TokenResponse response = tokenService.completeToken(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ===== Counters =====

    @GetMapping("/counters")
    public ResponseEntity<List<CounterStatusDTO>> getCounters() {
        List<CounterStatusDTO> counters = counterRepository.findAll().stream()
                .map(c -> CounterStatusDTO.builder()
                        .id(c.getId())
                        .counterNumber(c.getCounterNumber())
                        .status(c.getStatus().name())
                        .currentTokenNumber(c.getCurrentTokenNumber())
                        .tokensInQueue(c.getTokensInQueue())
                        .capacity(c.getCapacity())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(counters);
    }

    @PostMapping("/counters")
    public ResponseEntity<?> addCounter(@RequestBody Map<String, Object> request) {
        try {
            Long locationId = Long.valueOf(request.get("locationId").toString());
            Location location = locationRepository.findById(locationId)
                    .orElseThrow(() -> new RuntimeException("Location not found"));

            List<Counter> existing = counterRepository.findByLocationId(locationId);
            int nextNumber = existing.size() + 1;

            Counter counter = Counter.builder()
                    .counterNumber(nextNumber)
                    .location(location)
                    .status(Counter.CounterStatus.IDLE)
                    .tokensInQueue(0)
                    .capacity(20)
                    .build();

            counter = counterRepository.save(counter);

            return ResponseEntity.ok(CounterStatusDTO.builder()
                    .id(counter.getId())
                    .counterNumber(counter.getCounterNumber())
                    .status(counter.getStatus().name())
                    .tokensInQueue(0)
                    .capacity(20)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/counters/{id}")
    public ResponseEntity<?> updateCounter(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            Counter counter = counterRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Counter not found"));

            String status = request.get("status");
            if (status != null) {
                counter.setStatus(Counter.CounterStatus.valueOf(status));
            }

            counter = counterRepository.save(counter);

            return ResponseEntity.ok(CounterStatusDTO.builder()
                    .id(counter.getId())
                    .counterNumber(counter.getCounterNumber())
                    .status(counter.getStatus().name())
                    .currentTokenNumber(counter.getCurrentTokenNumber())
                    .tokensInQueue(counter.getTokensInQueue())
                    .capacity(counter.getCapacity())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/counters/{id}")
    public ResponseEntity<?> deleteCounter(@PathVariable Long id) {
        counterRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Counter deleted"));
    }

    // ===== Statistics =====

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics() {
        return ResponseEntity.ok(tokenService.getStatistics());
    }

    // ===== History =====

    @GetMapping("/history")
    public ResponseEntity<List<TokenResponse>> getHistory() {
        return ResponseEntity.ok(tokenService.getAllHistory());
    }

    // ===== DP Optimization =====

    @PostMapping("/optimize/{locationId}")
    public ResponseEntity<OptimizationResponse> optimize(@PathVariable Long locationId) {
        return ResponseEntity.ok(tokenService.runOptimization(locationId));
    }
}
