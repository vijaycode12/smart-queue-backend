package com.smartqueue.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        token.setCreatedAt(LocalDateTime.now());

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

    // Complete Token - only if SERVING
    public Token completeToken(Long id) {
        Token token = tokenRepository.findById(id).orElse(null);
        if (token != null && token.getStatus().equals("SERVING")) {
            token.setStatus("COMPLETED");
            token.setCompletedAt(LocalDateTime.now());
            return tokenRepository.save(token);
        }
        return null;
    }

    // Mark No Show - only if SERVING
    public Token markNoShow(Long id) {
        Token token = tokenRepository.findById(id).orElse(null);
        if (token != null && token.getStatus().equals("SERVING")) {
            token.setStatus("NO_SHOW");
            token.setCompletedAt(LocalDateTime.now());
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
}