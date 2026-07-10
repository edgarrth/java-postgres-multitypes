package pe.axiz.paymentprocessing.application.service;

import org.junit.jupiter.api.Test;
import pe.axiz.paymentprocessing.domain.model.DigitalCertificate;
import pe.axiz.paymentprocessing.domain.model.DigitalCertificateMetadata;
import pe.axiz.paymentprocessing.domain.port.out.DigitalCertificateRepositoryPort;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class DigitalCertificateServiceTest {
    @Test
    void shouldCalculateFingerprintAndStorePemAsByteaPayload() {
        var repository = new InMemoryCertificateRepository();
        var service = new DigitalCertificateService(repository);
        byte[] pem = "-----BEGIN PUBLIC KEY-----\nabc\n-----END PUBLIC KEY-----".getBytes(StandardCharsets.UTF_8);

        var certificate = service.register("demo", "processor", "RSA", pem);

        assertThat(certificate.fingerprintSha256()).hasSize(64);
        assertThat(service.getByAlias("demo").pemContent()).isEqualTo(pem);
    }

    static class InMemoryCertificateRepository implements DigitalCertificateRepositoryPort {
        private final Map<String, DigitalCertificate> data = new HashMap<>();

        @Override
        public DigitalCertificate save(DigitalCertificate certificate) {
            data.put(certificate.alias(), certificate);
            return certificate;
        }

        @Override
        public Optional<DigitalCertificate> findByAlias(String alias) {
            return Optional.ofNullable(data.get(alias));
        }

        @Override
        public List<DigitalCertificateMetadata> findAllMetadata() {
            return data.values().stream()
                    .map(c -> new DigitalCertificateMetadata(c.id(), c.alias(), c.owner(), c.algorithm(), c.fingerprintSha256(), c.pemContent().length, c.createdAt()))
                    .toList();
        }
    }
}
