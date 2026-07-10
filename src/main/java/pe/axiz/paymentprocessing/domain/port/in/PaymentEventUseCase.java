package pe.axiz.paymentprocessing.domain.port.in;

import pe.axiz.paymentprocessing.domain.model.EventStatus;
import pe.axiz.paymentprocessing.domain.model.PaymentOutboxEvent;
import pe.axiz.paymentprocessing.domain.model.PaymentEventNotification;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PaymentEventUseCase {
    PaymentOutboxEvent appendEvent(String aggregateId, String eventType, Map<String, Object> payload);
    List<PaymentOutboxEvent> findByStatus(EventStatus status);
    PaymentOutboxEvent markAsPublished(UUID id);
    List<PaymentEventNotification> findRecentNotifications(int limit);
}
