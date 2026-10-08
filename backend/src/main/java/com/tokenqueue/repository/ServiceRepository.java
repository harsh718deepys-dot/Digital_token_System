package com.tokenqueue.repository;

import com.tokenqueue.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByLocationId(Long locationId);
    List<ServiceEntity> findByLocationIdAndActiveTrue(Long locationId);
}
