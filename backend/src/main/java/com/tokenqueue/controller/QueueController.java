package com.tokenqueue.controller;

import com.tokenqueue.dto.TokenDTO.*;
import com.tokenqueue.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/{serviceId}")
    public ResponseEntity<QueueStatusResponse> getQueueStatus(@PathVariable Long serviceId) {
        return ResponseEntity.ok(tokenService.getQueueStatus(serviceId));
    }

    @GetMapping("/{serviceId}/status")
    public ResponseEntity<QueueStatusResponse> getQueueStatusShort(@PathVariable Long serviceId) {
        return ResponseEntity.ok(tokenService.getQueueStatus(serviceId));
    }
}
