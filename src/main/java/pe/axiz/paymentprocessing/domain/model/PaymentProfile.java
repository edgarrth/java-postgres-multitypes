package pe.axiz.paymentprocessing.domain.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record PaymentProfile(
        UUID id,
        String profileCode,
        String documentNumber,
        Map<String, Object> attributes,
        OffsetDateTime createdAt
) { }
