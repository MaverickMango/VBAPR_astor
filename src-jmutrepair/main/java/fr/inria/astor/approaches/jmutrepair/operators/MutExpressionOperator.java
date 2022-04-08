package fr.inria.astor.approaches.jmutrepair.operators;

import fr.inria.astor.approaches.jgenprog.extension.ExpressionProcessor;
import fr.inria.astor.approaches.jgenprog.operators.ReplaceOp;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.ExpresionMutOp;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.manipulation.MutationSupporter;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.QueueProcessingManager;

import java.util.ArrayList;
import java.util.List;

public class MutExpressionOperator extends ExpresionMutOp {
    List<CtExpression> expElements = new ArrayList<>();

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        CtElement ctExpression = point.getCodeElement();
        ExpressionProcessor processor = new ExpressionProcessor();
        QueueProcessingManager processingManager = new QueueProcessingManager(MutationSupporter.getFactory());
        processingManager.addProcessor(processor);
        processingManager.process(ctExpression);
        if (processor.expList.size() != 0) {
            expElements.addAll(new ArrayList<>(processor.expList));
            return true;
        }
        return false;
    }

    @Override
    protected OperatorInstance createModificationInstance(ModificationPoint point, MutantCtElement fix)
            throws IllegalAccessException {
        assert expElements.size() != 0;
//        OperatorInstance operation = new OperatorInstance();
//        operation.setOriginal(targetIF.getReturnedExpression());
//        operation.setOperationApplied(this);
//        operation.setModificationPoint(point);
//        operation.setModified(fix.getElement());

        return null;
    }

    /** Return the list of CtElements Mutanted */
    @Override
    public List<MutantCtElement> getMutants(CtElement element) {

        CtReturn targetIF = (CtReturn) element;
        List<MutantCtElement> mutations = null;
        mutations = this.mutatorBinary.execute(targetIF.getReturnedExpression());
        return mutations;
    }

}
