package pe.axiz.paymentprocessing.application.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeterministicEmbeddingServiceTest {
    private final DeterministicEmbeddingService service = new DeterministicEmbeddingService();

    @Test
    void shouldCreateStableSixDimensionEmbedding() {
        var first = service.embed("Pago QR de bajo monto");
        var second = service.embed(" pago qr   de bajo monto ");

        assertThat(first).hasSize(6);
        assertThat(first).isEqualTo(second);
    }
}
