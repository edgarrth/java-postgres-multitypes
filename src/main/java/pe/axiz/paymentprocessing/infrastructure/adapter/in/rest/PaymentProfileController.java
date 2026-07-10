package pe.axiz.paymentprocessing.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.axiz.paymentprocessing.domain.model.PaymentProfile;
import pe.axiz.paymentprocessing.domain.port.in.PaymentProfileUseCase;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment-profiles")
public class PaymentProfileController {
    private final PaymentProfileUseCase useCase;

    public PaymentProfileController(PaymentProfileUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PaymentProfile create(@Valid @RequestBody CreatePaymentProfileRequest request) {
        return useCase.registerProfile(request.profileCode(), request.documentNumber(), request.attributes());
    }

    @GetMapping
    List<PaymentProfile> findByAttribute(@RequestParam String attributeName, @RequestParam String value) {
        return useCase.findByAttribute(attributeName, value);
    }

    public record CreatePaymentProfileRequest(
            @NotBlank String profileCode,
            @NotBlank String documentNumber,
            @NotEmpty Map<String, Object> attributes
    ) { }
}
