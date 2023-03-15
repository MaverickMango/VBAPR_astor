package fr.inria.astor.approaches.jgenprog.extension;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.NegationUnaryOperatorConditionMutator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

import java.util.ArrayList;
import java.util.List;

public class NegationUnaryOperator4Neg extends NegationUnaryOperatorConditionMutator {

    public NegationUnaryOperator4Neg(Factory factory) {
        super(factory);
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<MutantCtElement>();

        if (toMutate instanceof CtUnaryOperator<?>) {
            CtUnaryOperator<?> unary = (CtUnaryOperator<?>) toMutate;
            if (unary.getKind() == UnaryOperatorKind.NEG) {
                CtExpression expIF = factory.Core().clone(unary.getOperand());
                expIF.setParent(unary.getParent());
                MutantCtElement mutatn = new MutantCtElement(expIF,0.3);
                //result.add(expIF);
                result.add(mutatn);
            }
        }
        return result;
    }
}
