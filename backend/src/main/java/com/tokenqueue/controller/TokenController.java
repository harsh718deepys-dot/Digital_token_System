package com.tokenqueue.controller;

import com.tokenqueue.dto.TokenDTO.*;
import com.tokenqueue.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity<?> createToken(@RequestBody CreateTokenRequest request, Authentication authentication) {
        try {
            TokenResponse response = tokenService.createToken(authentication.getName(), request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getToken(@PathVariable Long id) {
        try {
            TokenResponse response = tokenService.getToken(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/number/{tokenNumber}")
    public ResponseEntity<?> getTokenByNumber(@PathVariable String tokenNumber) {
        try {
            TokenResponse response = tokenService.getTokenByNumber(tokenNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveToken(Authentication authentication) {
        try {
            TokenResponse response = tokenService.getActiveToken(authentication.getName());
            return ResponseEntity.ok(response != null ? response : Map.of("message", "No active token"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelToken(@PathVariable Long id, Authentication authentication) {
        try {
            TokenResponse response = tokenService.cancelToken(id, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<TokenResponse>> getHistory(Authentication authentication) {
        return ResponseEntity.ok(tokenService.getVisitorHistory(authentication.getName()));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDTO>> getNotifications(Authentication authentication) {
        return ResponseEntity.ok(tokenService.getNotifications(authentication.getName()));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationRead(@PathVariable Long id) {
        tokenService.markNotificationRead(id);
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }
}
