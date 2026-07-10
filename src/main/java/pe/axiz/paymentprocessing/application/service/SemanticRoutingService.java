package pe.axiz.paymentprocessing.application.service;

import org.springframework.stereotype.Service;
import pe.axiz.paymentprocessing.domain.exception.DomainException;
import pe.axiz.paymentprocessing.domain.model.SemanticRule;
import pe.axiz.paymentprocessing.domain.model.SemanticRuleMatch;
import pe.axiz.paymentprocessing.domain.port.in.SemanticRoutingUseCase;
import pe.axiz.paymentprocessing.domain.port.out.SemanticRuleRepositoryPort;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SemanticRoutingService implements SemanticRoutingUseCase {
    private final SemanticRuleRepositoryPort repository;
    private final DeterministicEmbeddingService embeddingService;

    public SemanticRoutingService(SemanticRuleRepositoryPort repository, DeterministicEmbeddingService embeddingService) {
        this.repository = repository;
        this.embeddingService = embeddingService;
    }

    @Override
    public SemanticRule registerRule(String ruleCode, String title, String description) {
        requireText(ruleCode, "ruleCode");
        requireText(title, "title");
        requireText(description, "description");
        List<Double> embedding = embeddingService.embed(title + " " + description);
        return repository.save(new SemanticRule(UUID.randomUUID(), ruleCode, title, description, embedding, OffsetDateTime.now()));
    }

    @Override
    public List<SemanticRuleMatch> findNearestRules(String text, int limit) {
        requireText(text, "text");
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return repository.findNearest(embeddingService.embed(text), safeLimit);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainException(field + " is required");
        }
    }
}
