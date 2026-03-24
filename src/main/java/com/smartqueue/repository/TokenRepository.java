package com.smartqueue.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.smartqueue.model.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {

    List<Token> findByStatus(String status);

    @Query("SELECT MAX(t.tokenNumber) FROM Token t WHERE t.queue.id = :queueId")
    Integer findLastTokenNumber(@Param("queueId") Long queueId);

    @Query("SELECT t FROM Token t WHERE t.queue.id = :queueId ORDER BY t.tokenNumber ASC")
    List<Token> getTokensByQueue(@Param("queueId") Long queueId);

    @Query("SELECT t FROM Token t WHERE t.queue.id = :queueId AND t.status = 'WAITING' ORDER BY t.tokenNumber ASC")
    List<Token> findWaitingTokensByQueue(@Param("queueId") Long queueId);
    
    @Query("SELECT t FROM Token t WHERE t.user.id = :userId ORDER BY t.id DESC")
    List<Token> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT t FROM Token t WHERE t.status = 'COMPLETED' AND t.completedAt IS NOT NULL AND t.completedAt < :cutoff")
    List<Token> findCompletedOlderThan(@Param("cutoff") LocalDateTime cutoff);
 
    // ── Find ALL non-deleted tokens in a queue ordered by token number ──
    // Used after deletion to resequence token numbers
    @Query("SELECT t FROM Token t WHERE t.queue.id = :queueId ORDER BY t.tokenNumber ASC")
    List<Token> findAllByQueueIdOrderByTokenNumber(@Param("queueId") Long queueId);
 
    // ── Get distinct queue IDs that have completed tokens older than cutoff ──
    // So we only resequence queues that actually had deletions
    @Query("SELECT DISTINCT t.queue.id FROM Token t WHERE t.status = 'COMPLETED' AND t.completedAt IS NOT NULL AND t.completedAt < :cutoff")
    List<Long> findQueueIdsWithExpiredTokens(@Param("cutoff") LocalDateTime cutoff);
}