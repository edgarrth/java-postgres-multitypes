package pe.axiz.paymentprocessing.infrastructure.adapter.in.rest;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) { }
