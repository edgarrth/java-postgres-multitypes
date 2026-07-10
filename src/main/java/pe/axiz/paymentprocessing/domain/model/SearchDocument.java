package pe.axiz.paymentprocessing.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SearchDocument(
        UUID id,
        String paymentReference,
        String channel,
        String description,
        OffsetDateTime createdAt
) { }
