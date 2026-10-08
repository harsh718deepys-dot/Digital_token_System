package com.tokenqueue.repository;

import com.tokenqueue.model.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CounterRepository extends JpaRepository<Counter, Long> {
    List<Counter> findByLocationId(Long locationId);
    List<Counter> findByLocationIdAndStatusNot(Long locationId, Counter.CounterStatus status);
}
