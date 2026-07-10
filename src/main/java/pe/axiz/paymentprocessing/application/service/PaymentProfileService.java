package pe.axiz.paymentprocessing.application.service;

import org.springframework.stereotype.Service;
import pe.axiz.paymentprocessing.domain.exception.DomainException;
import pe.axiz.paymentprocessing.domain.model.PaymentProfile;
import pe.axiz.paymentprocessing.domain.port.in.PaymentProfileUseCase;
import pe.axiz.paymentprocessing.domain.port.out.PaymentProfileRepositoryPort;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentProfileService implements PaymentProfileUseCase {
    private final PaymentProfileRepositoryPort repository;

    public PaymentProfileService(PaymentProfileRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public PaymentProfile registerProfile(String profileCode, String documentNumber, Map<String, Object> attributes) {
        requireText(profileCode, "profileCode");
        requireText(documentNumber, "documentNumber");
        if (attributes == null || attributes.isEmpty()) {
            throw new DomainException("attributes is required");
        }
        return repository.save(new PaymentProfile(UUID.randomUUID(), profileCode, documentNumber, attributes, OffsetDateTime.now()));
    }

    @Override
    public List<PaymentProfile> findByAttribute(String attributeName, String value) {
        requireText(attributeName, "attributeName");
        requireText(value, "value");
        if (!attributeName.matches("[a-zA-Z0-9_]+")) {
            throw new DomainException("attributeName can only contain letters, numbers and underscore");
        }
        return repository.findByAttribute(attributeName, value);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainException(field + " is required");
        }
    }
}
