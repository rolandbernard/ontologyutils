package www.ontologyutils.toolbox;

import java.util.Collection;
import java.util.function.BiPredicate;

import org.semanticweb.owlapi.model.*;

/**
 * The preorder cache but extended to optimizing specially some constructs of
 * the logic.
 */
public class SubClassCache extends PreorderCache<OWLClassExpression> {
    /**
     * @param domain
     *            The domain of this preorder. May be incomplete.
     */
    public SubClassCache(Collection<OWLClassExpression> domain) {
        super();
        setupDomain(domain);
    }

    @Override
    protected boolean compute(OWLClassExpression pred, OWLClassExpression succ,
            BiPredicate<OWLClassExpression, OWLClassExpression> order) {
        if (pred.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
            if (pred.disjunctSet()
                    .anyMatch(p -> !isKnownSuccessor(p, succ) && !isPossibleSuccessor(p, succ))) {
                return false;
            } else if (pred.disjunctSet().allMatch(p -> isKnownSuccessor(p, succ))) {
                return true;
            }
        } else if (pred.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
            if (pred.conjunctSet().anyMatch(p -> isKnownSuccessor(p, succ))) {
                return true;
            }
        }
        if (succ.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
            if (succ.conjunctSet()
                    .anyMatch(s -> !isKnownSuccessor(pred, s) && !isPossibleSuccessor(pred, s))) {
                return false;
            } else if (succ.conjunctSet().allMatch(s -> isKnownSuccessor(pred, s))) {
                return true;
            }
        } else if (succ.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
            if (succ.disjunctSet().anyMatch(s -> isKnownSuccessor(pred, s))) {
                return true;
            }
        }
        return super.compute(pred, succ, order);
    }
}
