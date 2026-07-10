package pe.axiz.paymentprocessing.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.axiz.paymentprocessing.domain.model.DigitalCertificate;
import pe.axiz.paymentprocessing.domain.model.DigitalCertificateMetadata;
import pe.axiz.paymentprocessing.domain.port.in.DigitalCertificateUseCase;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-certificates")
public class DigitalCertificateController {
    private final DigitalCertificateUseCase useCase;

    public DigitalCertificateController(DigitalCertificateUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DigitalCertificateResponse create(@Valid @RequestBody CreateCertificateRequest request) {
        byte[] pem = Base64.getDecoder().decode(request.base64Pem());
        DigitalCertificate certificate = useCase.register(request.alias(), request.owner(), request.algorithm(), pem);
        return DigitalCertificateResponse.from(certificate, false);
    }

    @GetMapping("/{alias}")
    DigitalCertificateResponse get(@PathVariable String alias,
                                   @RequestParam(defaultValue = "false") boolean includePem) {
        return DigitalCertificateResponse.from(useCase.getByAlias(alias), includePem);
    }

    @GetMapping
    List<DigitalCertificateMetadata> list() {
        return useCase.listMetadata();
    }

    public record CreateCertificateRequest(
            @NotBlank String alias,
            @NotBlank String owner,
            @NotBlank String algorithm,
            @NotBlank String base64Pem
    ) { }

    public record DigitalCertificateResponse(
            String id,
            String alias,
            String owner,
            String algorithm,
            String fingerprintSha256,
            int sizeBytes,
            String pem
    ) {
        static DigitalCertificateResponse from(DigitalCertificate certificate, boolean includePem) {
            return new DigitalCertificateResponse(
                    certificate.id().toString(),
                    certificate.alias(),
                    certificate.owner(),
                    certificate.algorithm(),
                    certificate.fingerprintSha256(),
                    certificate.pemContent().length,
                    includePem ? new String(certificate.pemContent(), StandardCharsets.UTF_8) : null
            );
        }
    }
}
