package com.app.repository;

import com.app.entity.Siege;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiegeRepository extends JpaRepository<Siege, Long> {
    Optional<Siege> findByNom(String nom);
}
