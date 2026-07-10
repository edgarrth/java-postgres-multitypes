package pe.axiz.paymentprocessing.domain.model;

import java.time.OffsetDateTime;
import java.util.Map;

public record CacheEntry(
        String cacheKey,
        Map<String, Object> value,
        OffsetDateTime expiresAt,
        OffsetDateTime updatedAt
) {
    public boolean expired(OffsetDateTime now) {
        return expiresAt != null && expiresAt.isBefore(now);
    }
}
