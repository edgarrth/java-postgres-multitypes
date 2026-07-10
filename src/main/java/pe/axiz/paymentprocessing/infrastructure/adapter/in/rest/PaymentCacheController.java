package pe.axiz.paymentprocessing.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.axiz.paymentprocessing.domain.model.CacheEntry;
import pe.axiz.paymentprocessing.domain.port.in.PaymentCacheUseCase;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment-cache")
public class PaymentCacheController {
    private final PaymentCacheUseCase useCase;

    public PaymentCacheController(PaymentCacheUseCase useCase) {
        this.useCase = useCase;
    }

    @PutMapping("/{cacheKey}")
    @ResponseStatus(HttpStatus.CREATED)
    CacheEntry put(@PathVariable String cacheKey, @Valid @RequestBody PutCacheRequest request) {
        return useCase.put(cacheKey, request.value(), request.ttlSeconds());
    }

    @GetMapping("/{cacheKey}")
    CacheEntry get(@PathVariable String cacheKey) {
        return useCase.get(cacheKey);
    }

    @DeleteMapping("/expired")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void evictExpired() {
        useCase.evictExpired();
    }

    public record PutCacheRequest(
            @NotEmpty Map<String, Object> value,
            @Positive int ttlSeconds
    ) { }
}
