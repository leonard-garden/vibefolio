package com.vibefolio.persistence.repo;

import com.vibefolio.persistence.entity.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepo extends JpaRepository<PortfolioEntity, UUID> {

    Optional<PortfolioEntity> findByUsername(String username);

    Optional<PortfolioEntity> findByEmail(String email);

    Optional<PortfolioEntity> findByCvHash(String cvHash);
}
