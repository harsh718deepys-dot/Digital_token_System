package com.tokenqueue.repository;

import com.tokenqueue.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByActiveTrue();
    List<Location> findByType(String type);
}
