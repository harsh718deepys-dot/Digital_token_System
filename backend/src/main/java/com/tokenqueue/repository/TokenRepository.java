package com.tokenqueue.repository;

import com.tokenqueue.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenNumber(String tokenNumber);

    List<Token> findByVisitorIdOrderByCreatedAtDesc(Long visitorId);

    List<Token> findByServiceIdAndStatusInOrderByPriorityAscCreatedAtAsc(Long serviceId, List<Token.Status> statuses);

    List<Token> findByLocationIdAndStatusIn(Long locationId, List<Token.Status> statuses);

    List<Token> findByLocationIdAndStatus(Long locationId, Token.Status status);

    List<Token> findByStatusOrderByCreatedAtDesc(Token.Status status);

    @Query("SELECT t FROM Token t WHERE t.location.id = :locationId AND t.createdAt >= :startOfDay ORDER BY t.createdAt DESC")
    List<Token> findTodayTokensByLocation(@Param("locationId") Long locationId, @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT t FROM Token t WHERE t.createdAt >= :startOfDay ORDER BY t.createdAt DESC")
    List<Token> findTodayTokens(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(t) FROM Token t WHERE t.createdAt >= :startOfDay")
    int countTodayTokens(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(t) FROM Token t WHERE t.status = :status AND t.createdAt >= :startOfDay")
    int countTodayTokensByStatus(@Param("status") Token.Status status, @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(t) FROM Token t WHERE t.status = :status")
    int countByStatus(@Param("status") Token.Status status);

    @Query("SELECT t.category, COUNT(t) FROM Token t WHERE t.createdAt >= :startOfDay GROUP BY t.category")
    List<Object[]> countByCategoryToday(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT HOUR(t.createdAt), COUNT(t) FROM Token t WHERE t.createdAt >= :startOfDay GROUP BY HOUR(t.createdAt)")
    List<Object[]> countByHourToday(@Param("startOfDay") LocalDateTime startOfDay);

    List<Token> findByCounterIdAndStatus(Long counterId, Token.Status status);

    @Query("SELECT COUNT(t) FROM Token t WHERE t.service.id = :serviceId AND t.status IN ('WAITING', 'CALLED')")
    int countWaitingByService(@Param("serviceId") Long serviceId);
}
