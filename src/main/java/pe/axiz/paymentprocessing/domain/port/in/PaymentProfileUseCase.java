package pe.axiz.paymentprocessing.domain.port.in;

import pe.axiz.paymentprocessing.domain.model.PaymentProfile;
import java.util.List;
import java.util.Map;

public interface PaymentProfileUseCase {
    PaymentProfile registerProfile(String profileCode, String documentNumber, Map<String, Object> attributes);
    List<PaymentProfile> findByAttribute(String attributeName, String value);
}
