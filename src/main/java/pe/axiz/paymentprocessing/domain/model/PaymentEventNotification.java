package pe.axiz.paymentprocessing.domain.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record PaymentEventNotification(
        UUID id,
        UUID eventId,
        String channel,
        String action,
        String aggregateId,
        String eventType,
        EventStatus status,
        Map<String, Object> payload,
        OffsetDateTime receivedAt
) { }
