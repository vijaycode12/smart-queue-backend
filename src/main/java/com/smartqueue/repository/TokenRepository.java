package com.smartqueue.repository;

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
}