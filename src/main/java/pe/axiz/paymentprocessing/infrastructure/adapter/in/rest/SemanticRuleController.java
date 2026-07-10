package pe.axiz.paymentprocessing.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.axiz.paymentprocessing.domain.model.SemanticRule;
import pe.axiz.paymentprocessing.domain.model.SemanticRuleMatch;
import pe.axiz.paymentprocessing.domain.port.in.SemanticRoutingUseCase;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-semantic-rules")
public class SemanticRuleController {
    private final SemanticRoutingUseCase useCase;

    public SemanticRuleController(SemanticRoutingUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SemanticRule create(@Valid @RequestBody CreateSemanticRuleRequest request) {
        return useCase.registerRule(request.ruleCode(), request.title(), request.description());
    }

    @GetMapping("/nearest")
    List<SemanticRuleMatch> nearest(@RequestParam String text,
                                    @RequestParam(defaultValue = "5") int limit) {
        return useCase.findNearestRules(text, limit);
    }

    public record CreateSemanticRuleRequest(
            @NotBlank String ruleCode,
            @NotBlank String title,
            @NotBlank String description
    ) { }
}
