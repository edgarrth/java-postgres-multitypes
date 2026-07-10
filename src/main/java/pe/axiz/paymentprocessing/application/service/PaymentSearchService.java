package pe.axiz.paymentprocessing.application.service;

import org.springframework.stereotype.Service;
import pe.axiz.paymentprocessing.domain.exception.DomainException;
import pe.axiz.paymentprocessing.domain.model.SearchDocument;
import pe.axiz.paymentprocessing.domain.port.in.PaymentSearchUseCase;
import pe.axiz.paymentprocessing.domain.port.out.SearchDocumentRepositoryPort;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentSearchService implements PaymentSearchUseCase {
    private final SearchDocumentRepositoryPort repository;

    public PaymentSearchService(SearchDocumentRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public SearchDocument registerDocument(String paymentReference, String channel, String description) {
        requireText(paymentReference, "paymentReference");
        requireText(channel, "channel");
        requireText(description, "description");
        return repository.save(new SearchDocument(UUID.randomUUID(), paymentReference, channel, description, OffsetDateTime.now()));
    }

    @Override
    public List<SearchDocument> search(String query) {
        requireText(query, "query");
        return repository.search(query.trim());
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainException(field + " is required");
        }
    }
}
