package pe.axiz.paymentprocessing.domain.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record PaymentOutboxEvent(
        UUID id,
        String aggregateId,
        String eventType,
        Map<String, Object> payload,
        EventStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime publishedAt
) { }
