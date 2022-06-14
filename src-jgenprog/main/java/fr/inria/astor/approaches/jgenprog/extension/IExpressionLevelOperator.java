package fr.inria.astor.approaches.jgenprog.extension;

import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.CtExpression;

public interface IExpressionLevelOperator {
    public default boolean canBeAppliedToPoint(ModificationPoint point) {

        return (point.getCodeElement() instanceof CtExpression);
    }
}
