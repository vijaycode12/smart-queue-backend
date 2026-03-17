package com.smartqueue.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartqueue.model.Token;
import com.smartqueue.service.TokenService;

@RestController
@RequestMapping("/token")
@CrossOrigin
public class TokenController {

    @Autowired
    private TokenService tokenService;

    // USER - Generate Token
    @PostMapping("/generate/{queueId}")
    public ResponseEntity<?> generateToken(
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestHeader(value = "role", required = false) String role,
            @PathVariable Long queueId) {

        if (userId == null) {
            return ResponseEntity.status(401).body("Please login first.");
        }
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied.");
        }
        Token token = tokenService.generateToken(userId, queueId);
        if (token == null) {
            return ResponseEntity.status(400).body("Queue or User not found.");
        }
        return ResponseEntity.ok(token);
    }

    // ADMIN - Get All Tokens
    @GetMapping("/all")
    public ResponseEntity<?> getAllTokens(
            @RequestHeader(value = "role", required = false) String role) {

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied. Admins only.");
        }
        return ResponseEntity.ok(tokenService.getAllTokens());
    }

    // PUBLIC - Get Waiting Tokens
    @GetMapping("/waiting")
    public List<Token> getWaitingTokens() {
        return tokenService.getWaitingTokens();
    }

    // ADMIN - Complete Token
    @PutMapping("/complete/{id}")
    public ResponseEntity<?> completeToken(
            @RequestHeader(value = "role", required = false) String role,
            @PathVariable Long id) {

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied. Admins only.");
        }
        Token token = tokenService.completeToken(id);
        if (token == null) {
            return ResponseEntity.status(400).body("Token not found or not in SERVING state.");
        }
        return ResponseEntity.ok(token);
    }

    // ADMIN - No Show
    @PutMapping("/no-show/{id}")
    public ResponseEntity<?> markNoShow(
            @RequestHeader(value = "role", required = false) String role,
            @PathVariable Long id) {

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied. Admins only.");
        }
        Token token = tokenService.markNoShow(id);
        if (token == null) {
            return ResponseEntity.status(400).body("Token not found or not in SERVING state.");
        }
        return ResponseEntity.ok(token);
    }

    // PUBLIC - Get Tokens By Queue
    @GetMapping("/queue/{queueId}")
    public List<Token> getTokensByQueue(@PathVariable Long queueId) {
        return tokenService.getTokensByQueue(queueId);
    }

    // USER - Queue Position
    @GetMapping("/position/{tokenId}")
    public ResponseEntity<?> getQueuePosition(
            @RequestHeader(value = "userId", required = false) Long userId,
            @PathVariable Long tokenId) {

        if (userId == null) {
            return ResponseEntity.status(401).body("Please login first.");
        }
        int position = tokenService.getQueuePosition(tokenId);
        if (position == -1) {
            return ResponseEntity.status(404).body("Token not found or already served.");
        }
        return ResponseEntity.ok("Your position: " + position + " (" + (position - 1) + " people ahead)");
    }

    // ADMIN - Call Next Token
    @PutMapping("/call-next/{queueId}")
    public ResponseEntity<?> callNextToken(
            @RequestHeader(value = "role", required = false) String role,
            @PathVariable Long queueId) {

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied. Admins only.");
        }
        Token token = tokenService.callNextToken(queueId);
        if (token == null) {
            return ResponseEntity.status(404).body("No waiting tokens in this queue.");
        }
        return ResponseEntity.ok(token);
    }

    // USER - Estimated Wait Time
    @GetMapping("/wait-time/{tokenId}")
    public ResponseEntity<?> getEstimatedWaitTime(@PathVariable Long tokenId) {
        return ResponseEntity.ok(tokenService.getEstimatedWaitTime(tokenId));
    }
}