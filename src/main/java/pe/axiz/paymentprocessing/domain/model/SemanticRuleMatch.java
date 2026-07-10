package pe.axiz.paymentprocessing.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SemanticRuleMatch(
        UUID id,
        String ruleCode,
        String title,
        String description,
        double distance,
        OffsetDateTime createdAt
) { }
