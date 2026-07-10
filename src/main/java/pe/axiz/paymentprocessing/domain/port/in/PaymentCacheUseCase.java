package pe.axiz.paymentprocessing.domain.port.in;

import pe.axiz.paymentprocessing.domain.model.CacheEntry;
import java.util.Map;

public interface PaymentCacheUseCase {
    CacheEntry put(String cacheKey, Map<String, Object> value, int ttlSeconds);
    CacheEntry get(String cacheKey);
    void evictExpired();
}
