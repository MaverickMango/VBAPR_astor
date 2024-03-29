package fr.inria.astor.approaches.jgenprog.extension;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;

import java.util.ArrayList;
import java.util.List;

public class ExpressionProcessor extends AbstractProcessor<CtExpression> {
    public List<CtExpression> expList = new ArrayList<>();

    @Override
    public void process(CtExpression ctExpression) {
        if (ctExpression instanceof CtNewArray
                || ctExpression instanceof CtTypeAccess || ctExpression instanceof CtThisAccess
                || ctExpression instanceof CtVariableAccess || ctExpression instanceof CtLiteral) {
            return;
        }
        expList.add(ctExpression);
    }
}
