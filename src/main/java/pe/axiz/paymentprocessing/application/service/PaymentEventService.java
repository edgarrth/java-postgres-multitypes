package pe.axiz.paymentprocessing.application.service;

import org.springframework.stereotype.Service;
import pe.axiz.paymentprocessing.domain.exception.DomainException;
import pe.axiz.paymentprocessing.domain.exception.NotFoundException;
import pe.axiz.paymentprocessing.domain.model.EventStatus;
import pe.axiz.paymentprocessing.domain.model.PaymentOutboxEvent;
import pe.axiz.paymentprocessing.domain.model.PaymentEventNotification;
import pe.axiz.paymentprocessing.domain.port.in.PaymentEventUseCase;
import pe.axiz.paymentprocessing.domain.port.out.PaymentEventRepositoryPort;
import pe.axiz.paymentprocessing.domain.port.out.PaymentEventNotificationRepositoryPort;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentEventService implements PaymentEventUseCase {
    private final PaymentEventRepositoryPort repository;
    private final PaymentEventNotificationRepositoryPort notificationRepository;

    public PaymentEventService(PaymentEventRepositoryPort repository,
                               PaymentEventNotificationRepositoryPort notificationRepository) {
        this.repository = repository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public PaymentOutboxEvent appendEvent(String aggregateId, String eventType, Map<String, Object> payload) {
        requireText(aggregateId, "aggregateId");
        requireText(eventType, "eventType");
        if (payload == null || payload.isEmpty()) {
            throw new DomainException("payload is required");
        }
        return repository.save(new PaymentOutboxEvent(UUID.randomUUID(), aggregateId, eventType, payload, EventStatus.PENDING, OffsetDateTime.now(), null));
    }

    @Override
    public List<PaymentOutboxEvent> findByStatus(EventStatus status) {
        return repository.findByStatus(status == null ? EventStatus.PENDING : status);
    }

    @Override
    public PaymentOutboxEvent markAsPublished(UUID id) {
        if (id == null) {
            throw new DomainException("id is required");
        }
        return repository.markAsPublished(id)
                .orElseThrow(() -> new NotFoundException("Event not found: " + id));
    }

    @Override
    public List<PaymentEventNotification> findRecentNotifications(int limit) {
        if (limit < 1 || limit > 100) {
            throw new DomainException("limit must be between 1 and 100");
        }
        return notificationRepository.findRecent(limit);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainException(field + " is required");
        }
    }
}
