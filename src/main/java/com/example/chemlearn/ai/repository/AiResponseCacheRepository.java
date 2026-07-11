package com.example.chemlearn.ai.repository;

import com.example.chemlearn.ai.entity.AiResponseCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiResponseCacheRepository extends JpaRepository<AiResponseCache, UUID> {
    Optional<AiResponseCache> findByCacheKey(String cacheKey);
}
