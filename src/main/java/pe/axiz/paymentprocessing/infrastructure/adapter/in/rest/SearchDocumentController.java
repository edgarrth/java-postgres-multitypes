package pe.axiz.paymentprocessing.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.axiz.paymentprocessing.domain.model.SearchDocument;
import pe.axiz.paymentprocessing.domain.port.in.PaymentSearchUseCase;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-search-documents")
public class SearchDocumentController {
    private final PaymentSearchUseCase useCase;

    public SearchDocumentController(PaymentSearchUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SearchDocument create(@Valid @RequestBody CreateSearchDocumentRequest request) {
        return useCase.registerDocument(request.paymentReference(), request.channel(), request.description());
    }

    @GetMapping
    List<SearchDocument> search(@RequestParam String query) {
        return useCase.search(query);
    }

    public record CreateSearchDocumentRequest(
            @NotBlank String paymentReference,
            @NotBlank String channel,
            @NotBlank String description
    ) { }
}
