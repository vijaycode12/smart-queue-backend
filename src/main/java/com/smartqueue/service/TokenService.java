package com.smartqueue.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartqueue.model.Queue;
import com.smartqueue.model.Token;
import com.smartqueue.model.User;
import com.smartqueue.repository.QueueRepository;
import com.smartqueue.repository.TokenRepository;
import com.smartqueue.repository.UserRepository;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private UserRepository userRepository;

    // Generate Token
    public Token generateToken(Long userId, Long queueId) {
        Queue queue = queueRepository.findById(queueId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (queue == null || user == null) return null;

        Integer lastToken = tokenRepository.findLastTokenNumber(queueId);
        int nextToken = (lastToken == null) ? 1 : lastToken + 1;

        Token token = new Token();
        token.setQueue(queue);
        token.setUser(user);
        token.setTokenNumber(nextToken);
        token.setStatus("WAITING");
        token.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        return tokenRepository.save(token);
    }

    // Get All Tokens
    public List<Token> getAllTokens() {
        return tokenRepository.findAll();
    }

    // Get Waiting Tokens
    public List<Token> getWaitingTokens() {
        return tokenRepository.findByStatus("WAITING");
    }

 // ── NEW: Get all tokens belonging to a specific user ──
    public List<Token> getTokensByUser(Long userId) {
        return tokenRepository.findByUserId(userId);
    }
 
    
    // Complete Token - only if SERVING
    public Token completeToken(Long id) {
        Token token = tokenRepository.findById(id).orElse(null);
        if (token != null && token.getStatus().equals("SERVING")) {
            token.setStatus("COMPLETED");
            token.setCompletedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            return tokenRepository.save(token);
        }
        return null;
    }

    // Mark No Show - only if SERVING
    public Token markNoShow(Long id) {
        Token token = tokenRepository.findById(id).orElse(null);
        if (token != null && token.getStatus().equals("SERVING")) {
            token.setStatus("NO_SHOW");
            token.setCompletedAt(LocalDateTime.now((ZoneId.of("Asia/Kolkata"))));
            return tokenRepository.save(token);
        }
        return null;
    }

    // Get Tokens By Queue
    public List<Token> getTokensByQueue(Long queueId) {
        return tokenRepository.getTokensByQueue(queueId);
    }

    // Queue Position
    public int getQueuePosition(Long tokenId) {
        Token token = tokenRepository.findById(tokenId).orElse(null);
        if (token == null) return -1;

        // Check if token is still WAITING
        if (!token.getStatus().equals("WAITING")) {
            return -1;
        }

        List<Token> waitingTokens = tokenRepository.findWaitingTokensByQueue(
            token.getQueue().getId()
        );

        int position = 1;
        for (Token t : waitingTokens) {
            if (t.getId().equals(tokenId)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    // Call Next Token
    public Token callNextToken(Long queueId) {
        List<Token> waiting = tokenRepository.findWaitingTokensByQueue(queueId);
        if (waiting.isEmpty()) return null;
        Token next = waiting.get(0);
        next.setStatus("SERVING");
        return tokenRepository.save(next);
    }

    // Estimated Wait Time
    public String getEstimatedWaitTime(Long tokenId) {
        Token token = tokenRepository.findById(tokenId).orElse(null);
        if (token == null) return "Token not found";

        // If token is not WAITING, no wait time
        if (!token.getStatus().equals("WAITING")) {
            return "Your token is: " + token.getStatus();
        }

        List<Token> waitingTokens = tokenRepository.findWaitingTokensByQueue(
            token.getQueue().getId()
        );

        int peopleAhead = 0;
        for (Token t : waitingTokens) {
            if (t.getId().equals(tokenId)) {
                break;
            }
            peopleAhead++;
        }

        int avgServiceTime = 5;
        int waitTime = peopleAhead * avgServiceTime;

        if (waitTime == 0) {
            return "You are next! Estimated wait time: 0 minutes";
        }

        return "Estimated wait time: " + waitTime + " minutes (" + peopleAhead + " people ahead)";
    }
    
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Kolkata") // every hour on the hour
    @Transactional
    public void autoDeleteExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneId.of("Asia/Kolkata")).minusHours(24);
 
        // Step 1: Find which queues will be affected BEFORE deleting
        List<Long> affectedQueueIds = tokenRepository.findQueueIdsWithExpiredTokens(cutoff);
 
        if (affectedQueueIds.isEmpty()) {
            System.out.println("[SmartQueue] Token cleanup: nothing to delete.");
            return;
        }
 
        // Step 2: Find and delete expired completed tokens
        List<Token> expired = tokenRepository.findCompletedOlderThan(cutoff);
        int count = expired.size();
        tokenRepository.deleteAll(expired);
        System.out.println("[SmartQueue] Token cleanup: deleted " + count + " completed token(s) older than 24h.");
 
        // Step 3: Resequence token numbers in each affected queue
        for (Long queueId : affectedQueueIds) {
            resequenceTokenNumbers(queueId);
        }
 
        System.out.println("[SmartQueue] Token resequencing complete for " + affectedQueueIds.size() + " queue(s).");
    }
 
    /**
     * Resequences all remaining tokens in a queue by tokenNumber starting from 1.
     * Preserves original order — only renumbers.
     *
     * Example queue state after deletion:
     *   DB rows: tokenNumber=2 (WAITING), tokenNumber=3 (WAITING)
     *   After resequence: tokenNumber=1 (WAITING), tokenNumber=2 (WAITING)
     */
    @Transactional
    public void resequenceTokenNumbers(Long queueId) {
        List<Token> remaining = tokenRepository.findAllByQueueIdOrderByTokenNumber(queueId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setTokenNumber(i + 1);
        }
        tokenRepository.saveAll(remaining);
    }
 
    // ─────────────────────────────────────────────
    // MANUAL TRIGGER (for testing / admin use)
    // ─────────────────────────────────────────────
    @Transactional
    public String manualCleanup() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneId.of("Asia/Kolkata")).minusHours(24);
        List<Long> affectedQueueIds = tokenRepository.findQueueIdsWithExpiredTokens(cutoff);
        List<Token> expired = tokenRepository.findCompletedOlderThan(cutoff);
        int count = expired.size();
        if (count == 0) return "No completed tokens older than 24h found.";
        tokenRepository.deleteAll(expired);
        for (Long queueId : affectedQueueIds) {
            resequenceTokenNumbers(queueId);
        }
        return "Deleted " + count + " token(s) and resequenced " + affectedQueueIds.size() + " queue(s).";
    }
}