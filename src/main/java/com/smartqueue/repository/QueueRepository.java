package com.smartqueue.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartqueue.model.Queue;

public interface QueueRepository extends JpaRepository<Queue, Long> {

}
