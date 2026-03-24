package com.smartqueue.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.smartqueue.model.Queue;

public interface QueueRepository extends JpaRepository<Queue, Long> {

	List<Queue> findAllByOrderByDisplayIdAsc();
	 
    // Get max displayId currently in use
    @Query("SELECT MAX(q.displayId) FROM Queue q")
    Integer findMaxDisplayId();
 
    // Get queues that are still ACTIVE or PAUSED (for auto-inactive scheduler)
    @Query("SELECT q FROM Queue q WHERE q.status = 'ACTIVE' OR q.status = 'PAUSED'")
    List<Queue> findActiveOrPaused();
}
