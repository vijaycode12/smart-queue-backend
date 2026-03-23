package com.smartqueue.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartqueue.model.Queue;
import com.smartqueue.repository.QueueRepository;
import com.smartqueue.service.QueueService;

@RestController
@RequestMapping("/queue")
@CrossOrigin
public class QueueController {

    @Autowired
    private QueueService queueService;
    private QueueRepository queueRepository;

    // ADMIN ONLY - create queue
    @PostMapping("/create")
    public ResponseEntity<?> createQueue(
            @RequestHeader(value = "role", required = false) String role,
            @RequestBody Queue queue) {

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied. Admins only.");
        }
        return ResponseEntity.ok(queueService.createQueue(queue));
    }

    // PUBLIC - anyone can see queues
    @GetMapping("/all")
    public List<Queue> getAllQueues() {
        return queueService.getAllQueues();
    }
    
    @PutMapping("/status/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                           @RequestParam String status,
                                           @RequestHeader("role") String role) {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(403).body("Access denied");
        try {
            Queue updated = queueService.updateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
 
    // Delete queue — ADMIN only
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteQueue(@PathVariable Long id,
                                          @RequestHeader("role") String role) {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(403).body("Access denied");
        try {
            queueService.deleteQueue(id);
            return ResponseEntity.ok("Queue deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}