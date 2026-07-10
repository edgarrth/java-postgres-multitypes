package pe.axiz.paymentprocessing.application.service;

import org.springframework.stereotype.Service;
import pe.axiz.paymentprocessing.domain.exception.DomainException;
import pe.axiz.paymentprocessing.domain.exception.NotFoundException;
import pe.axiz.paymentprocessing.domain.model.DigitalCertificate;
import pe.axiz.paymentprocessing.domain.model.DigitalCertificateMetadata;
import pe.axiz.paymentprocessing.domain.port.in.DigitalCertificateUseCase;
import pe.axiz.paymentprocessing.domain.port.out.DigitalCertificateRepositoryPort;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class DigitalCertificateService implements DigitalCertificateUseCase {
    private final DigitalCertificateRepositoryPort repository;

    public DigitalCertificateService(DigitalCertificateRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public DigitalCertificate register(String alias, String owner, String algorithm, byte[] pemContent) {
        requireText(alias, "alias");
        requireText(owner, "owner");
        requireText(algorithm, "algorithm");
        if (pemContent == null || pemContent.length == 0) {
            throw new DomainException("pemContent is required");
        }
        String pem = new String(pemContent);
        if (!pem.contains("BEGIN CERTIFICATE") && !pem.contains("BEGIN PUBLIC KEY")) {
            throw new DomainException("pemContent must look like a PEM certificate/public key");
        }
        String fingerprint = sha256(pemContent);
        return repository.save(new DigitalCertificate(UUID.randomUUID(), alias.trim(), owner.trim(), algorithm.trim(), fingerprint, pemContent, OffsetDateTime.now()));
    }

    @Override
    public DigitalCertificate getByAlias(String alias) {
        requireText(alias, "alias");
        return repository.findByAlias(alias.trim())
                .orElseThrow(() -> new NotFoundException("Certificate not found: " + alias));
    }

    @Override
    public List<DigitalCertificateMetadata> listMetadata() {
        return repository.findAllMetadata();
    }

    private static String sha256(byte[] value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainException(field + " is required");
        }
    }
}
