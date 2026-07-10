package pe.axiz.paymentprocessing.domain.port.in;

import pe.axiz.paymentprocessing.domain.model.SemanticRule;
import pe.axiz.paymentprocessing.domain.model.SemanticRuleMatch;
import java.util.List;

public interface SemanticRoutingUseCase {
    SemanticRule registerRule(String ruleCode, String title, String description);
    List<SemanticRuleMatch> findNearestRules(String text, int limit);
}
