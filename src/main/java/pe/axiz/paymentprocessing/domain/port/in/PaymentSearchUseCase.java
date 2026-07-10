package pe.axiz.paymentprocessing.domain.port.in;

import pe.axiz.paymentprocessing.domain.model.SearchDocument;
import java.util.List;

public interface PaymentSearchUseCase {
    SearchDocument registerDocument(String paymentReference, String channel, String description);
    List<SearchDocument> search(String query);
}
