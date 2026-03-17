package com.smartqueue.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartqueue.model.Queue;
import com.smartqueue.repository.QueueRepository;

@Service
public class QueueService {

    @Autowired
    private QueueRepository queueRepository;

    public Queue createQueue(Queue queue){

        queue.setCreatedAt(LocalDateTime.now().toString());

        return queueRepository.save(queue);
    }

    public List<Queue> getAllQueues(){
        return queueRepository.findAll();
    }
    
//    public Queue updateStatus(Long id, String status) {
//        Queue queue = queueRepository.findById(id)
//            .orElseThrow(() -> new RuntimeException("Queue not found"));
//        queue.setStatus(status);
//        return queueRepository.save(queue);
//    }
//
//    public void deleteQueue(Long id) {
//        queueRepository.deleteById(id);
//    }
}