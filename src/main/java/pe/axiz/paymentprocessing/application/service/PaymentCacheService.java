package pe.axiz.paymentprocessing.application.service;

import org.springframework.stereotype.Service;
import pe.axiz.paymentprocessing.domain.exception.DomainException;
import pe.axiz.paymentprocessing.domain.exception.NotFoundException;
import pe.axiz.paymentprocessing.domain.model.CacheEntry;
import pe.axiz.paymentprocessing.domain.port.in.PaymentCacheUseCase;
import pe.axiz.paymentprocessing.domain.port.out.CacheEntryRepositoryPort;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class PaymentCacheService implements PaymentCacheUseCase {
    private final CacheEntryRepositoryPort repository;

    public PaymentCacheService(CacheEntryRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public CacheEntry put(String cacheKey, Map<String, Object> value, int ttlSeconds) {
        if (cacheKey == null || cacheKey.isBlank()) {
            throw new DomainException("cacheKey is required");
        }
        if (value == null || value.isEmpty()) {
            throw new DomainException("value is required");
        }
        int safeTtl = Math.min(Math.max(ttlSeconds, 30), 86_400);
        OffsetDateTime now = OffsetDateTime.now();
        return repository.upsert(new CacheEntry(cacheKey.trim(), value, now.plusSeconds(safeTtl), now));
    }

    @Override
    public CacheEntry get(String cacheKey) {
        CacheEntry entry = repository.findByKey(cacheKey)
                .orElseThrow(() -> new NotFoundException("Cache entry not found: " + cacheKey));
        if (entry.expired(OffsetDateTime.now())) {
            repository.deleteExpired();
            throw new NotFoundException("Cache entry expired: " + cacheKey);
        }
        return entry;
    }

    @Override
    public void evictExpired() {
        repository.deleteExpired();
    }
}
