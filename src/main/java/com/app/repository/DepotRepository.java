package com.app.repository;

import com.app.entity.Depot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepotRepository extends JpaRepository<Depot, Long> {
    List<Depot> findAllBySiegeId(Long siegeId);
}
