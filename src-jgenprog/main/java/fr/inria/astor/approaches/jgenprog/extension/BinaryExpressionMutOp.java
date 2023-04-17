package fr.inria.astor.approaches.jgenprog.extension;

import fr.inria.astor.approaches.jgenprog.VariableReferenceProcessor;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.ExpresionMutOp;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.QueueProcessingManager;

import java.util.ArrayList;
import java.util.List;

public class BinaryExpressionMutOp extends ExpressionMutOp {
    @Override
    protected OperatorInstance createModificationInstance(ModificationPoint point, MutantCtElement fix) throws IllegalAccessException {
        CtElement element = point.getCodeElement();
        List<CtBinaryOperator> list1 = element.getElements(new TypeFilter<>(CtBinaryOperator.class));
        List<CtUnaryOperator> list2 = element.getElements(new TypeFilter<>(CtUnaryOperator.class));
        OperatorInstance operation = new OperatorInstance();
        if (list1.size() > 0)
            operation.setOriginal(list1.get(0));
        else if (list2.size() > 0)
            operation.setOriginal(list2.get(0));
        operation.setOperationApplied(this);
        operation.setModificationPoint(point);
        operation.setModified(fix.getElement());

        return operation;
    }

    /**
     * Return the list of CtElements Mutanted
     *
     * @param element
     */
    @Override
    public List<MutantCtElement> getMutants(CtElement element) {
        List<MutantCtElement> mutations;
        if (ConfigurationProperties.getPropertyBool("extractSubVar")) {
            mutations = this.mutatorBinary.execute(element);
            return mutations;
        }
        mutations = new ArrayList<>();
        List<CtBinaryOperator> list1 = element.getElements(new TypeFilter<>(CtBinaryOperator.class));
        List<CtUnaryOperator> list2 = element.getElements(new TypeFilter<>(CtUnaryOperator.class));
        if (list1.size() > 0)
            mutations.addAll(this.mutatorBinary.execute(list1.get(0)));
        if (list2.size() > 0)
            mutations.addAll(this.mutatorBinary.execute(list2.get(0)));
        return mutations;
    }

    /**
     * Indicates whether the operator can be applied in the ModificationPoint
     * passed as argument.
     * <p>
     * By default, we consider that an operator works at the level of
     * CtStatement.
     *
     * @param point location to modify
     * @return
     */
    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        //due to the modificationpoint selection strategy selects all elements of different levels, here is a redundancy to filter all sub-elements of itself.
//        boolean flag = (point.getCodeElement().getElements(new TypeFilter(CtBinaryOperator.class)).size() > 0
//                || point.getCodeElement().getElements(new TypeFilter(CtUnaryOperator.class)).size() > 0);
        return point.getCodeElement() instanceof CtBinaryOperator || point.getCodeElement() instanceof CtUnaryOperator;
    }
}
