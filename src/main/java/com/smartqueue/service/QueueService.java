package com.smartqueue.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartqueue.model.Queue;
import com.smartqueue.repository.QueueRepository;

@Service
public class QueueService {

    @Autowired
    private QueueRepository queueRepository;

    public Queue createQueue(Queue queue){

    	queue.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
    	
    	if (queue.getStatus() == null || queue.getStatus().isEmpty()) {
            queue.setStatus("ACTIVE");
        }
 
        // Assign next displayId
        Integer maxId = queueRepository.findMaxDisplayId();
        queue.setDisplayId(maxId == null ? 1 : maxId + 1);
        

        return queueRepository.save(queue);
    }

    public List<Queue> getAllQueues(){
        return queueRepository.findAll();
    }
    
    public Queue updateStatus(Long id, String status) {
        Queue queue = queueRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Queue not found with id: " + id));
 
        // Validate allowed statuses
        if (!status.equals("ACTIVE") && !status.equals("INACTIVE") && !status.equals("PAUSED")) {
            throw new RuntimeException("Invalid status. Allowed: ACTIVE, INACTIVE, PAUSED");
        }
 
        queue.setStatus(status);
        return queueRepository.save(queue);
    }
 
    // Delete a queue by ID
//    @Transactional
//    public void deleteQueue(Long id) {
//        if (!queueRepository.existsById(id)) {
//            throw new RuntimeException("Queue not found with id: " + id);
//        }
// 
//        // Delete the queue
//        queueRepository.deleteById(id);
// 
//        // Re-sequence all remaining queues by displayId order
//        List<Queue> remaining = queueRepository.findAllByOrderByDisplayIdAsc();
//        for (int i = 0; i < remaining.size(); i++) {
//            remaining.get(i).setDisplayId(i + 1);
//        }
//        queueRepository.saveAll(remaining);
//    }
    
    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void autoInactiveAt6PM() {
        List<Queue> queues = queueRepository.findActiveOrPaused();
        for (Queue q : queues) {
            q.setStatus("INACTIVE");
        }
        queueRepository.saveAll(queues);
        System.out.println("[SmartQueue] 10:00 PM — All active queues set to INACTIVE automatically.");
    }
}