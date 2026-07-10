package pe.axiz.paymentprocessing.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DigitalCertificate(
        UUID id,
        String alias,
        String owner,
        String algorithm,
        String fingerprintSha256,
        byte[] pemContent,
        OffsetDateTime createdAt
) { }
