package com.civicpilot.civicpilot.repository;

import com.civicpilot.civicpilot.model.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchemeRepository extends JpaRepository<Scheme, Long> {
    Optional<Scheme> findByCode(String code);
}
