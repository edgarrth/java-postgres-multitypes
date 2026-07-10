package pe.axiz.paymentprocessing.domain.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SemanticRule(
        UUID id,
        String ruleCode,
        String title,
        String description,
        List<Double> embedding,
        OffsetDateTime createdAt
) { }
