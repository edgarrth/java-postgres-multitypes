package pe.axiz.paymentprocessing.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DigitalCertificateMetadata(
        UUID id,
        String alias,
        String owner,
        String algorithm,
        String fingerprintSha256,
        int sizeBytes,
        OffsetDateTime createdAt
) { }
