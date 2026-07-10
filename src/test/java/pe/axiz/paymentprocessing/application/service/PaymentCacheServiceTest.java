package pe.axiz.paymentprocessing.application.service;

import org.junit.jupiter.api.Test;
import pe.axiz.paymentprocessing.domain.exception.NotFoundException;
import pe.axiz.paymentprocessing.domain.model.CacheEntry;
import pe.axiz.paymentprocessing.domain.port.out.CacheEntryRepositoryPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentCacheServiceTest {
    @Test
    void shouldStoreAndGetCacheEntry() {
        var repository = new InMemoryCacheRepository();
        var service = new PaymentCacheService(repository);

        service.put("merchant-risk:MRC-1", Map.of("decision", "ALLOW"), 60);

        CacheEntry entry = service.get("merchant-risk:MRC-1");
        assertThat(entry.value()).containsEntry("decision", "ALLOW");
    }

    @Test
    void shouldThrowWhenCacheEntryDoesNotExist() {
        var service = new PaymentCacheService(new InMemoryCacheRepository());

        assertThatThrownBy(() -> service.get("missing"))
                .isInstanceOf(NotFoundException.class);
    }

    static class InMemoryCacheRepository implements CacheEntryRepositoryPort {
        private final Map<String, CacheEntry> data = new HashMap<>();

        @Override
        public CacheEntry upsert(CacheEntry entry) {
            data.put(entry.cacheKey(), entry);
            return entry;
        }

        @Override
        public Optional<CacheEntry> findByKey(String cacheKey) {
            return Optional.ofNullable(data.get(cacheKey));
        }

        @Override
        public void deleteExpired() {
            data.entrySet().removeIf(entry -> entry.getValue().expired(java.time.OffsetDateTime.now()));
        }
    }
}
