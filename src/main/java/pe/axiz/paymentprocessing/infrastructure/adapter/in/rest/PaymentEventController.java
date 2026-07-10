package pe.axiz.paymentprocessing.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.axiz.paymentprocessing.domain.model.EventStatus;
import pe.axiz.paymentprocessing.domain.model.PaymentOutboxEvent;
import pe.axiz.paymentprocessing.domain.model.PaymentEventNotification;
import pe.axiz.paymentprocessing.domain.port.in.PaymentEventUseCase;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment-events")
public class PaymentEventController {
    private final PaymentEventUseCase useCase;

    public PaymentEventController(PaymentEventUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PaymentOutboxEvent append(@Valid @RequestBody AppendPaymentEventRequest request) {
        return useCase.appendEvent(request.aggregateId(), request.eventType(), request.payload());
    }

    @GetMapping
    List<PaymentOutboxEvent> findByStatus(@RequestParam(defaultValue = "PENDING") EventStatus status) {
        return useCase.findByStatus(status);
    }

    @PatchMapping("/{id}/published")
    PaymentOutboxEvent markPublished(@PathVariable UUID id) {
        return useCase.markAsPublished(id);
    }

    @GetMapping("/notifications")
    List<PaymentEventNotification> findRecentNotifications(@RequestParam(defaultValue = "20") int limit) {
        return useCase.findRecentNotifications(limit);
    }

    public record AppendPaymentEventRequest(
            @NotBlank String aggregateId,
            @NotBlank String eventType,
            @NotEmpty Map<String, Object> payload
    ) { }
}
